package userInterface.swing;

import javax.swing.JComponent;
import java.awt.*;

public class RoomPreviewPanel extends JComponent {
    RoomSnapshot snapshot;

    public RoomPreviewPanel(RoomSnapshot snapshot) {
        this.snapshot = snapshot;
        setPreferredSize(new Dimension(200, 160));
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        try {
            RenderState state = new RenderState(false, false, false, true);
            RoomPanelPainter.paint(g, this, snapshot, state);
        } finally {
            g.dispose();
        }
    }  

}
