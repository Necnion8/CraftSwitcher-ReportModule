package com.gmail.necnionch.myapp.craftswitcherreportmodule.socket;

import com.gmail.necnionch.myapp.craftswitcherreportmodule.CraftSwitcherReporter;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.data.*;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.exceptions.ClosedError;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.exceptions.ResponseError;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.utils.CompleteFuture;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.utils.SimpleTimer;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.FutureListener;

import java.nio.charset.StandardCharsets;
import java.util.*;


public class TCPClient {
    private Bootstrap bootstrap;
    private NioEventLoopGroup workGroup = new NioEventLoopGroup();
    private boolean reconnectFlag;
    private boolean authorized;
    private TimerTask reconnectTimer;
    private int sendDataId;
    private Map<Integer, CompleteFuture> sendCompleteFutures = new HashMap<>();
    private ChannelHandlerContext channelContext;
    private List<OnClientListener> onClientListeners = new ArrayList<>();

    static {
        SerializableData.putType("login", LoginData.class);
        SerializableData.putType("invalid", InvalidData.class);
        SerializableData.putType("empty-response", EmptyResponseData.class);
        SerializableData.putType("status", StatusData.class);

        SerializableData.putType("server-stop-request", ServerStopRequest.class);
        SerializableData.putType("server-start-request", ServerStartRequest.class);
        SerializableData.putType("server-restart-request", ServerRestartRequest.class);
        SerializableData.putType("server-list-request", ServerListRequest.class);
        SerializableData.putType("server-change-state", ServerChangeStateData.class);
        SerializableData.putType("server-state-request", ServerStateRequest.class);
        SerializableData.putType("server-add", ServerAddData.class);
        SerializableData.putType("server-remove", ServerRemoveData.class);
    }

    public TCPClient() {
        bootstrap = new Bootstrap();
        bootstrap.group(workGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.config().setConnectTimeoutMillis(1000);
                socketChannel.pipeline().addLast(
                        new StringDecoder(StandardCharsets.UTF_8),
                        new StringEncoder(StandardCharsets.UTF_8),
                        new LineBasedFrameDecoder(2048),
                        new ChannelInboundHandlerAdapter() {
                            StringBuilder sb = new StringBuilder();
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object obj) {
                                sb.append(obj.toString());

                                int splitIndex = sb.indexOf("\n");
                                String line;
//                                System.out.println("All: " + sb.toString());
                                while (splitIndex != -1) {
                                    line = sb.substring(0, splitIndex);
                                    sb.delete(0, splitIndex+1);
                                    splitIndex = sb.indexOf("\n");
//                                    System.out.println("out: " + line);
                                    try {
                                        onChannelReadLine(ctx, line);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                System.out.println("Exception: " + cause.getMessage());
                                cause.printStackTrace();
//                                super.exceptionCaught(ctx, cause);
                            }

                            @Override
                            public void channelActive(ChannelHandlerContext ctx) {
                                channelContext = ctx;
                                String serverName = CraftSwitcherReporter.getInstance().getServerName();
                                ctx.channel().writeAndFlush("###AUTH:CraftSwitcher1:REPORTER:" + serverName + "###\n");
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) {
                                new ArrayList<>(onClientListeners).forEach(l -> {
                                    try {
                                        l.onDisconnectClient();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });

                                if (reconnectFlag) {
                                    reconnectTimer = SimpleTimer.schedule(() -> {
                                        reconnectTimer = null;
                                        connect();
                                    }, (authorized) ? 1000 : 1000 * 120);

                                }
                            }

                        }
                );
            }
        });

    }

    public void connect() {
        disconnect();
        cancelReconnectTimer();
        reconnectFlag = true;
        authorized = false;
        sendDataId = 0;
        sendCompleteFutures.values().forEach(f -> f.callFail(new ClosedError()));
        sendCompleteFutures.clear();

        final ChannelFuture f = bootstrap.connect("127.0.0.1", 8023);
        f.addListener((FutureListener<Void>) voidFuture -> {
            if (!f.isSuccess() && reconnectFlag) {
                reconnectTimer = SimpleTimer.schedule(() -> {
                    reconnectTimer = null;
                    connect();
                }, 1000 * 5);
            }
        });
    }

    public void disconnect() {
        cancelReconnectTimer();

        if (channelContext != null) {
            reconnectFlag = false;
            try {
                channelContext.disconnect().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            authorized = false;
            channelContext = null;
        }
    }

    public void cancelReconnectTimer() {
        if (reconnectTimer != null) {
            reconnectTimer.cancel();
            reconnectTimer = null;
        }
    }

    public void addListener(OnClientListener listener) {
        this.onClientListeners.add(listener);
    }

    public void removeListener(OnClientListener listener) {
        this.onClientListeners.remove(listener);
    }

    public boolean isAvailable() {
        return channelContext != null && channelContext.channel().isActive() && authorized;
    }



    public synchronized CompleteFuture sendFuture(SerializableData data) {
        if (isAvailable()) {
            sendDataId++;
            final int id = sendDataId;

            CompleteFuture future = new CompleteFuture(() -> {
                channelContext.writeAndFlush(
                        String.join("," , new String[] {
                                "send",
                                SerializableData.TYPES_TO_KEY.get(data.getClass()),
                                String.valueOf(id),
                                new Gson().toJson(data)
                        }) + "\n"
                );
            });
            sendCompleteFutures.put(id, future);
            return future;

        } else {
            throw new UnsupportedOperationException("client is closed");
        }
    }

    private void sendResponse(SerializableData data, int dataId) {
        if (isAvailable()) {
            channelContext.writeAndFlush(
                    String.join(",", new String[] {
                            "response",
                            SerializableData.TYPES_TO_KEY.get(data.getClass()),
                            String.valueOf(dataId),
                            new Gson().toJson(data)
                    }) + "\n"
            );
        }
    }


    private void processReceiveData(SerializableData data, int dataId) {
        SerializableData responseData = null;

        for (OnClientListener listener : (new ArrayList<>(onClientListeners))) {
            try {
                responseData = listener.onReceiveData(data, dataId);
            } catch (Exception e) {
                e.printStackTrace();
                responseData = new InvalidData("internal-error");
            }
            if (responseData != null)
                break;
        }

        if (responseData == null)
            responseData = new EmptyResponseData();

        sendResponse(responseData, dataId);

    }

    private void processResponseData(SerializableData data, int dataId) {
        if (sendCompleteFutures.containsKey(dataId)) {
            if (data instanceof InvalidData) {
                sendCompleteFutures.remove(dataId).callFail(new ResponseError(((InvalidData) data).message));
            } else {
                sendCompleteFutures.remove(dataId).callDone(data);
            }
        }
    }


    private void onChannelReadLine(ChannelHandlerContext ctx, String msg) {
//        System.out.println("readLine: " + msg);
        if (!authorized) {
            if (msg.equals("###AUTH:CraftSwitcher1###")) {
                authorized = true;
//                System.out.println("auth!");
                new ArrayList<>(onClientListeners).forEach(l -> {
                    try {
                        l.onConnectClient();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } else {
                System.err.println("Unsupported server! (Auth: " + msg + ")");
                ctx.disconnect();
            }
            return;
        }

        String[] split = msg.split(",", 4);
        if (split.length != 4) {
            System.out.println("invalid data lengths -> " + split.length);
            return;
        }

        String method = split[0];
        String key = split[1];
        int dataId = Integer.parseInt(split[2]);
        String jsonRaw = split[3];
        Class<? extends SerializableData> dataType = SerializableData.getClass(key);
        if (dataType != null) {
            SerializableData data;
            try {
                data = new Gson().fromJson(jsonRaw, dataType);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                System.err.println("raw-out: \"" + msg + "\"");
                return;
            }

            if (method.equals("send")) {
                processReceiveData(data, dataId);
                return;

            } else if (method.equals("response")) {
                processResponseData(data, dataId);
                return;
            }

        } else if (method.equals("send")) {
            sendResponse(new InvalidData("unknown data-type: " + key), dataId);
            return;

        }

        System.out.println("warn received: \"" + msg + "\"");
    }


    public void setReconnectFlag(boolean reconnectFlag) {
        this.reconnectFlag = reconnectFlag;
    }

    public boolean isReconnectFlag() {
        return reconnectFlag;
    }


    public interface OnClientListener {
        default SerializableData onReceiveData(SerializableData data, int dataId) { return null; }
        default void onConnectClient() {}
        default void onDisconnectClient() {}

    }

}
