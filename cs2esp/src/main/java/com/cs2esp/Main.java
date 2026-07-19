package com.cs2esp;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;

public class Main {

    static String processName = "cs2.exe";
    static int screenWidth;
    static int screenHeight;

    public static void main(String[] args){
        MemoryUtils memoryUtils = new MemoryUtils();
        RECT gameRect = new RECT();

        if (!memoryUtils.open(processName)) {
            System.out.println("Cannot open cs2.exe !");
            return;
        }

        int pid = memoryUtils.findProcessId(processName);
        HWND hwnd = memoryUtils.getWindowHandle(pid);

        if (hwnd == null) {
            System.out.println("Can't find window of cs2.exe !");
            return;
        }
        
        if (!User32.INSTANCE.GetWindowRect(hwnd, gameRect)) {
            System.out.println("Can't get window rect of cs2.exe !");
            return;
        }

        screenWidth = gameRect.right - gameRect.left;
        screenHeight = gameRect.bottom - gameRect.top;
        long baseAddress = memoryUtils.getModuleBase(pid, "client.dll");

        if (baseAddress == 0) {
            System.out.println("Can't get base address of client.dll !");
            return;
        }
        //Print some base infomation
        System.out.println("Screen Width: " + screenWidth + ", Screen Height: " + screenHeight);
        System.out.println("Game Window Rect: " + gameRect.left + ", " + gameRect.top + ", " + gameRect.right + ", " + gameRect.bottom);
        System.out.println("Process ID: " + pid);
        System.out.println("Base Address: " + Long.toHexString(baseAddress));

        Overlay overlay = new Overlay(screenWidth, screenHeight);
        overlay.setVisible(true);
        //Always loop
        while (true) {
            ArrayList<Entity> entities = new ArrayList<>();
            Entity localPlayer = new Entity();
            Long pLocalPlayer = memoryUtils.readPointer(baseAddress + Offset.dwLocalPlayerController);
            if (pLocalPlayer == null || pLocalPlayer == 0) { 
                continue; 
            }

            int localTeamNum = memoryUtils.readInt(pLocalPlayer + Offset.m_iTeamNum);
            Long pLocalPlayerPawn = memoryUtils.readPointer(baseAddress + Offset.dwLocalPlayerPawn);
            if (pLocalPlayerPawn == null || pLocalPlayerPawn == 0) {
                continue; 
            }

            Vector3 localPlayerPosition = memoryUtils.readVector3(pLocalPlayerPawn + Offset.m_vOldOrigin);
            if (localPlayerPosition == null) { 
                continue; 
            }

            float[] viewMatrix = memoryUtils.readMatrix4x4(baseAddress + Offset.dwViewMatrix);
            localPlayer.teamNum = localTeamNum;
            localPlayer.position = localPlayerPosition;
            Long pEntity_list = memoryUtils.readPointer(baseAddress + Offset.dwEntityList);

            for (int i = 1; i < 32; i++) { 
                Entity entity = new Entity();
                Long list_entry = memoryUtils.readPointer(pEntity_list + (8 * (i & 0x7FFF) >> 9) + 16);    
                if (list_entry == null | list_entry == 0) { 
                    continue; 
                }
                Long pPlayer = memoryUtils.readPointer(list_entry + (i + 1) * 0x70);
                if (pPlayer == null || pPlayer == 0) { 
                    continue; 
                }
                boolean isAlive = memoryUtils.readInt(pPlayer + Offset.m_bPawnIsAlive) != 0;
                if (!isAlive) { 
                    continue; 
                }
                int teamNum = memoryUtils.readInt(pPlayer + Offset.m_iTeamNum);
                if (teamNum == localTeamNum) { 
                    continue; 
                }
                String playerName = memoryUtils.readString(pPlayer + Offset.m_iszPlayerName,128);
                if (playerName == null || playerName.isEmpty()) { 
                    continue; 
                }      
                Long playerPawn = memoryUtils.readPointer(pPlayer + Offset.m_hPlayerPawn);
                Long list_entry2 = memoryUtils.readPointer(pEntity_list + 0x8 * ((playerPawn & 0x7FFF) >> 9) + 16);
                if (list_entry2 == null || list_entry2 == 0) { 
                    continue; 
                }
                Long pCSPlayerPawn = memoryUtils.readPointer(list_entry2 + 0x70 * (playerPawn & 0x1FF));
                if (pCSPlayerPawn == pLocalPlayer) { 
                    continue; 
                }
                int playerHealth = memoryUtils.readInt(pCSPlayerPawn + Offset.m_iHealth);
                if (playerHealth < 0 || playerHealth > 255) { 
                    continue; 
                }
                int playerArmor = memoryUtils.readInt(pCSPlayerPawn + Offset.m_ArmorValue);
                if (playerArmor < 0 || playerArmor > 255) { 
                    continue; 
                }
                Vector3 playerPosition = memoryUtils.readVector3(pCSPlayerPawn + Offset.m_vOldOrigin);
                if (playerPosition == null) { 
                    continue; 
                }
                Vector3 playerView = memoryUtils.readVector3(pCSPlayerPawn + Offset.m_vecViewOffset);
                if (playerView == null) {
                    continue;
                }

                entity.name = playerName;
                entity.health = playerHealth;
                entity.armor = playerArmor;
                entity.teamNum = teamNum;
                entity.pawn = pCSPlayerPawn;
                entity.position = playerPosition;

                entities.add(entity);
            }
           
            BufferedImage frame = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = frame.createGraphics();
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.8f));
            g2d.setStroke(new BasicStroke(2));

            for (Entity entity : entities) {
                Vector3 origin = entity.position;
                Vector3 head = new Vector3();
                head.x = origin.x;
                head.y = origin.y;
                head.z = origin.z + 75.f;

                Vector3 screenPos = Utils.worldToScreen(origin, viewMatrix, screenWidth, screenHeight);
                Vector3 screenHead = Utils.worldToScreen(head, viewMatrix, screenWidth, screenHeight);
                
                if (screenPos != null && screenHead != null) {
                    float height = screenPos.y - screenHead.y;
                    float width = height / 2.4f;
                    float x =  screenHead.x - width / 2; 
                    float y =  screenHead.y;

                    //Draw player name
                    Font myFont = new Font("SansSerif", Font.BOLD, 13);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(myFont);
                    g2d.drawString(entity.name, x, y - 15);
                    //Draw Box
                    g2d.setColor(new Color(1f, 0f, 0f, 0.1f));
                    g2d.fillRect((int) x, (int) y, (int) width, (int) height);
                    g2d.setColor(Color.RED);
                    g2d.drawRect((int) x, (int) y, (int) width, (int) height);
                    //Draw line to the player box
                    g2d.drawLine(screenWidth / 2, screenHeight, (int) ((int)x + width / 2), (int) (y  + height));
                    //Draw health bar
                    g2d.setColor(Color.GREEN);
                    g2d.drawRect((int) x - 6, (int) y + (int)(height * (100 - entity.health) / 100), (int)1.5, (int) height - (int)(height * (100 - entity.health) / 100));
                    //Draw health text
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(myFont);
                    g2d.drawString(String.valueOf(entity.health), x, y - 3);
                    //Draw armor bar
                    g2d.setColor(Color.WHITE);
                    g2d.drawRect((int) x - 12, (int) y + (int)(height * (100 - entity.armor) / 100), (int)1.5, (int) height - (int)(height * (100 - entity.armor) / 100));

                }
            }
            g2d.dispose();
            javax.swing.SwingUtilities.invokeLater(() -> {
                overlay.updateFrame(frame);
            });
        }
    }
}