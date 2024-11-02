package com.gmail.necnionch.myapp.craftswitcherreportmodule;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.lang.instrument.Instrumentation;

public class CraftSwitcherAgent {
    private static ClassPool classPool;

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        String serverName = "";
        String[] args = (agentArgs != null) ? agentArgs.split(",") : new String[0];
        if (args.length >= 1) {
            serverName = args[0];
        }

        new CraftSwitcherReporter(serverName);

        classPool = ClassPool.getDefault();
        instrumentation.addTransformer((loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
            if (className == null)
                return null;

            try {
                ByteArrayInputStream is;
                CtClass ctClass;
                CtMethod ctMethod;

                switch (className) {
                    case "org/bukkit/Bukkit":
                        is = new ByteArrayInputStream(classfileBuffer);
                        ctClass = classPool.makeClass(is);

                        ctMethod = ctClass.getDeclaredMethod("setServer");
                        ctMethod.insertAfter(
                                "com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.bukkit.BukkitReporter.init();"
                        );
                        return ctClass.toBytecode();

                    case "org/bukkit/command/SimpleCommandMap":
                        is = new ByteArrayInputStream(classfileBuffer);
                        ctClass = classPool.makeClass(is);

                        ctMethod = ctClass.getDeclaredMethod("setDefaultCommands");
                        ctMethod.insertAfter(
                                "com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.bukkit.BukkitReporter.onSetDefaultCommands();"
                        );
                        return ctClass.toBytecode();

                    case "net/md_5/bungee/api/ProxyServer":
                        is = new ByteArrayInputStream(classfileBuffer);
                        ctClass = classPool.makeClass(is);

                        ctMethod = ctClass.getDeclaredMethod("setInstance");
                        ctMethod.insertAfter(
                                "com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.bungee.BungeeReporter.init();"
                        );
                        return ctClass.toBytecode();

                    case "net/md_5/bungee/BungeeCord":
                        is = new ByteArrayInputStream(classfileBuffer);
                        ctClass = classPool.makeClass(is);
                        ctMethod = ctClass.getDeclaredMethod("start");
                        ctMethod.insertAfter(
                                "com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.bungee.BungeeReporter.onInitServer();"
                        );
                        return ctClass.toBytecode();

                    case "net/minecraft/server/dedicated/DedicatedServer":
                        is = new ByteArrayInputStream(classfileBuffer);
                        ctClass = classPool.makeClass(is);
                        ctMethod = ctClass.getDeclaredMethod("init");
                        ctMethod.insertAfter(
                                "com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.bukkit.BukkitReporter.onInitServer();"
                        );
                        return ctClass.toBytecode();


                    default:
                        if (className.matches("^net/minecraft/server/v.+/DedicatedServer$")) {
                            is = new ByteArrayInputStream(classfileBuffer);
                            ctClass = classPool.makeClass(is);
                            ctMethod = ctClass.getDeclaredMethod("init");
                            ctMethod.insertAfter(
                                    "com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.bukkit.BukkitReporter.onInitServer();"
                            );
                            return ctClass.toBytecode();
                        }
                        return null;
                }
            }  catch (Exception ex) {
                System.err.println("CraftSwitcher Report Agent -> Reflection Failed!");
                System.err.println(">> " + ex.getMessage());
                CraftSwitcherReporter.agentFailed();
                return null;
            }
        });

    }
}
