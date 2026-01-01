package userInterface.swing;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import util.DoorState;
import util.Position;

public class RoomPanel extends JPanel {

    @FunctionalInterface
    public interface RoomClickListener {
        void onRoomClicked(Position position, MouseEvent e);
    }

    private final Position position;
    private RoomSnapshot snapshot;

    private boolean isCurrentRoom = false;
    private boolean isSelected = false;

    private boolean hovered = false;
    private RoomClickListener clickListener;

    public RoomPanel(Position position) {
        this.position = position;
        snapshot = new RoomSnapshot(false, 0, "", false, new DoorState[]{DoorState.NONE, DoorState.NONE, DoorState.NONE, DoorState.NONE}, new int[]{0,0,0,0});
        setOpaque(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (clickListener != null) {
                    clickListener.onRoomClicked(position, e);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    public Position getPosition() {
        return position;
    }

    public RoomSnapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(RoomSnapshot snapshot) {
        if (snapshot == null) {
            snapshot = new RoomSnapshot(false, 0, "", false, new DoorState[]{DoorState.NONE, DoorState.NONE, DoorState.NONE, DoorState.NONE}, new int[]{0,0,0,0});
        }
        this.snapshot = snapshot;
        repaint();
    }
    
    public void setCurrent(boolean isCurrent) {
        this.isCurrentRoom = isCurrent;
        repaint();
    }
    
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        repaint();
    }

    public void setClickListener(RoomClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    protected void paintComponent(java.awt.Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            Insets in = getInsets();
            int x0 = in.left;
            int y0 = in.top;
            int x1 = w - in.right;
            int y1 = h - in.bottom;

            paintBackground(g, x0, y0, x1 - x0, y1 - y0);
            paintBorderEmphasis(g, x0, y0, x1 - x0, y1 - y0);
            paintRoomText(g, x0, y0, x1 - x0, y1 - y0);
            paintDoorTicks(g, x0, y0, x1 - x0, y1 - y0);

        } finally {
            g.dispose();
        }
    }

    private void paintBackground(Graphics2D g, int x0, int y0, int w, int h) {
        if (isSelected) {
            g.setColor(new Color(40, 80, 200, 35));
        } else {
            g.setColor(Color.WHITE);
        }
        g.fillRect(x0, y0, w, h);
    }

    private void paintBorderEmphasis(Graphics2D g, int x0, int y0, int w, int h) {
        if (isCurrentRoom) {
            g.setStroke(new BasicStroke(3f));
            g.setColor(new Color(70, 110, 200));
        } else if (snapshot.isGoalRoom()) {
            g.setStroke(new BasicStroke(2f));
            g.setColor(new Color(238, 192, 76));
        } else {
            g.setStroke(new BasicStroke(1f));
            g.setColor(new Color(180, 180, 180));
        }
        if (hovered) {
            g.setColor(g.getColor().darker());
        }
        g.drawRect(x0, y0, w - 1, h - 1);
    }

    private void paintRoomText(Graphics2D g, int x0, int y0, int w, int h) {
        if (snapshot.hasRoom()) {
            g.setColor(new Color(0, 0, 0, 180));
            g.setFont(getFont().deriveFont(Font.PLAIN, 11f));
            String num = String.valueOf(snapshot.roomNumber());
            FontMetrics fmNum = g.getFontMetrics();
            g.drawString(num, x0 + 6, y0 + 6 + fmNum.getAscent());

            g.setColor(Color.BLACK);
            g.setFont(getFont().deriveFont(Font.BOLD, 11f));
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

    private String[] shortened(FontMetrics fontMetrics, String roomName, int maxWidth) {
        String[] name = roomName.split(" ");
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

    private void paintDoorTicks(Graphics2D g, int x0, int y0, int w, int h) {
        if (snapshot.hasRoom()) {
            int min = Math.min(w, h);
            int tickLen = Math.max(8, (int) (min * 0.18));
            int stroke = Math.max(2, (int) (min * 0.03));

            g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = x0 + w / 2;
            int cy = y0 + h / 2;

            int margin = Math.max(4, stroke + 2);

            drawDoor(g, 0, cx, y0 + margin, cx, y0 + margin + tickLen, tickLen, stroke);          //N
            drawDoor(g, 1, x0 + w - margin, cy, x0 + w - margin - tickLen, cy, tickLen, stroke);  //E
            drawDoor(g, 2, cx, y0 + h - margin, cx, y0 + h - margin - tickLen, tickLen, stroke);  //S
            drawDoor(g, 3, x0 + margin, cy, x0 + margin + tickLen, cy, tickLen, stroke);          //W
        }
    }

    private void drawDoor(Graphics2D g, int i, int x1, int y1, int x2, int y2, int tickLen, int stroke) {
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

        // overlay indicators
        if (doorState == DoorState.LOCKED) {
            drawNotch(g, x1, y1, x2, y2, stroke + 3);
        } else if (doorState == DoorState.BLOCKED) {
            drawX(g, x1, y1, x2, y2, stroke + 4);
        }

        g.setStroke(original);
    }

    private Color colorForDoorState(DoorState doorState) {
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

    private void drawNotch(Graphics2D g, int x1, int y1, int x2, int y2, int i) {
        int mx = (x1 + x2) / 2;
        int my = (y1 + y2) / 2;
        g.fillRect(mx - i / 2, my - i / 2, i, i);
    }

    private void drawX(Graphics2D g, int x1, int y1, int x2, int y2, int i) {
        int mx = (x1 + x2) / 2;
        int my = (y1 + y2) / 2;
        int r = i / 2;
        g.drawLine(mx - r, my - r, mx + r, my + r);
        g.drawLine(mx - r, my + r, mx + r, my - r);
    }
}