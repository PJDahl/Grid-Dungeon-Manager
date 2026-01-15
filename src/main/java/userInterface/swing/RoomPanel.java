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
        setFont(new Font("Georgia", Font.PLAIN, 11));

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
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        try {
            RenderState state = new RenderState(isCurrentRoom, isSelected, hovered, false);
            RoomPanelPainter.paint(g, this, snapshot, state);
        } finally {
            g.dispose();
        }
    }
}