package userInterface.swing;

import java.awt.*;
import java.util.Arrays;

import javax.swing.JComponent;

import util.DoorState;

public class RoomPanelPainter {
    private static Font defaultTextFont = new Font("Georgia", Font.BOLD, 13);
    private static Font defaultNumberFont = new Font("Georgia", Font.PLAIN, 11);
    
    public static void paint(Graphics g0, JComponent panel, RoomSnapshot snapshot, RenderState state) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = panel.getWidth();
        int h = panel.getHeight();
        Insets in = panel.getInsets();
        int x0 = in.left;
        int y0 = in.top;
        int x1 = w - in.right;
        int y1 = h - in.bottom;

        paintBackground(g, state, x0, y0, x1 - x0, y1 - y0);
        paintBorderEmphasis(g, state, snapshot.isGoalRoom(), x0, y0, x1 - x0, y1 - y0);
        paintRoomText(g, panel, snapshot, x0, y0, x1 - x0, y1 - y0);
        paintDoorTicks(g, panel, snapshot, x0, y0, x1 - x0, y1 - y0);
    }

    private static void paintBackground(Graphics2D g, RenderState state, int x0, int y0, int w, int h) {
        if (state.isSelected()) {
            g.setColor(new Color(40, 80, 200, 35));
        } else {
            g.setColor(Color.WHITE);
        }
        g.fillRect(x0, y0, w, h);
    }

    private static void paintBorderEmphasis(Graphics2D g, RenderState state, boolean isGoalRoom, int x0, int y0, int w, int h) {
        if (state.isPreview()) {
            g.setStroke(new BasicStroke(3f));
            g.setColor(new Color(150, 150, 150));
        } else if (state.isCurrentRoom()) {
            g.setStroke(new BasicStroke(3f));
            g.setColor(new Color(70, 110, 200));
        } else if (isGoalRoom) {
            g.setStroke(new BasicStroke(2f));
            g.setColor(new Color(238, 192, 76));
        } else {
            g.setStroke(new BasicStroke(1f));
            g.setColor(new Color(180, 180, 180));
        }
        if (state.isHovered()) {
            g.setStroke(new BasicStroke(4f));
            g.setColor(g.getColor().darker());
        }
        g.drawRect(x0, y0, w - 1, h - 1);
    }

    private static void paintRoomText(Graphics2D g, JComponent panel, RoomSnapshot snapshot, int x0, int y0, int w, int h) {
        if (snapshot.hasRoom()) {
            g.setColor(new Color(0, 0, 0, 180));
            g.setFont(defaultNumberFont);
            String num = String.valueOf(snapshot.roomNumber());
            FontMetrics fmNum = g.getFontMetrics();
            g.drawString(num, x0 + 6, y0 + 6 + fmNum.getAscent());

            g.setColor(Color.BLACK);
            g.setFont(defaultTextFont);
            FontMetrics fm = g.getFontMetrics();
            String[] lines = shortened(fm, snapshot.roomName(), w - 36);

            int lineHeight = fm.getHeight();
            int totalTextHeight = lines.length * lineHeight;
            int startY = y0 + (h - totalTextHeight) / 2 + fm.getAscent();

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                int tx = x0 + (w - fm.stringWidth(line)) / 2; // center each line
                int ty = startY + i * lineHeight;
                g.drawString(line, tx, ty);
            }
        }
    }

    private static String[] shortened(FontMetrics fontMetrics, String roomName, int maxWidth) {
        String[] name = roomName.split(" ");
        if (name.length > 3) {
            name = Arrays.copyOfRange(name, 0, 4);
            name[3] = "...";
        }
        for (int i = 0; i < name.length; i++) {
            String part = name[i];
            if (fontMetrics.stringWidth(part) <= maxWidth) {
                continue;
            }
            while (part.length() > 0) {
                part = part.substring(0, part.length() - 1);
                String testName = part + "...";
                if (fontMetrics.stringWidth(testName) <= maxWidth) {
                    name[i] = testName;
                    break;
                }
            }
        }
        return name;
    }

    private static void paintDoorTicks(Graphics2D g, JComponent panel, RoomSnapshot snapshot, int x0, int y0, int w, int h) {
        if (snapshot.hasRoom()) {
            int min = Math.min(w, h);
            int tickLen = Math.max(8, (int) (min * 0.18));
            int stroke = Math.max(2, (int) (min * 0.03));

            g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = x0 + w / 2;
            int cy = y0 + h / 2;

            int margin = Math.max(4, stroke + 2);

            drawDoor(g, snapshot, 0, cx, y0 + margin, cx, y0 + margin + tickLen, stroke);          //N
            drawDoor(g, snapshot, 1, x0 + w - margin, cy, x0 + w - margin - tickLen, cy, stroke);  //E
            drawDoor(g, snapshot, 2, cx, y0 + h - margin, cx, y0 + h - margin - tickLen, stroke);  //S
            drawDoor(g, snapshot, 3, x0 + margin, cy, x0 + margin + tickLen, cy, stroke);          //W
        }
    }

    private static void drawDoor(Graphics2D g, RoomSnapshot snapshot, int i, int x1, int y1, int x2, int y2, int stroke) {
        DoorState doorState = snapshot.doors()[i];
        int neighbour = snapshot.neighbours()[i];
        Stroke original = g.getStroke();
        if (doorState == DoorState.NONE) {
            return;
        }
        
        if (doorState == DoorState.OPEN && neighbour == 0) {
            g.setStroke(new BasicStroke(
                    Math.max(2, stroke),
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND,
                    1f,
                    new float[]{6f, 6f},
                    0f
            ));
        }

        g.setColor(colorForDoorState(doorState));
        g.drawLine(x1, y1, x2, y2);

        if (doorState == DoorState.LOCKED) {
            drawNotch(g, x1, y1, x2, y2, stroke + 3);
        } else if (doorState == DoorState.BLOCKED) {
            drawX(g, x1, y1, x2, y2, stroke + 4);
        }

        g.setStroke(original);
    }

    private static Color colorForDoorState(DoorState doorState) {
        switch (doorState) {
            case OPEN:
                return new Color(40, 140, 60, 190);
            case LOCKED:
                return new Color(160, 20, 160, 190);
            case BLOCKED:
                return new Color(160, 20, 20, 220);
            default:
                return Color.BLACK;
        }
    }

    private static void drawNotch(Graphics2D g, int x1, int y1, int x2, int y2, int i) {
        int mx = (x1 + x2) / 2;
        int my = (y1 + y2) / 2;
        g.fillRect(mx - i / 2, my - i / 2, i, i);
    }

    private static void drawX(Graphics2D g, int x1, int y1, int x2, int y2, int i) {
        int mx = (x1 + x2) / 2;
        int my = (y1 + y2) / 2;
        int r = i / 2;
        g.drawLine(mx - r, my - r, mx + r, my + r);
        g.drawLine(mx - r, my + r, mx + r, my - r);
    }
}
