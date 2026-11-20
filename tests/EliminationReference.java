package gauss;

import java.awt.Event;

/** Drives the original buttons and reads the augmented matrix after each operation. */
public final class EliminationReference {
    private static void setMatrix(InPanel panel, double[][] values) {
        for (int i = 1; i <= panel.m; i++) {
            for (int j = 1; j <= panel.n; j++) {
                panel.A[i][j].x = values[i - 1][j - 1];
            }
            panel.B[i].x = values[i - 1][panel.n];
        }
    }

    private static void snapshot(InPanel panel) {
        for (int i = 1; i <= panel.m; i++) {
            for (int j = 1; j <= panel.n; j++) {
                System.out.print(panel.A[i][j].fromNumberField() + ",");
            }
            System.out.println(panel.B[i].fromNumberField());
        }
        System.out.println("---");
    }

    public static void main(String[] args) {
        InPanel panel = new InPanel(3, 4);
        setMatrix(panel, new double[][]{
            {0, 5, -2, 1, 7},
            {3, 1, 0, 5, 10},
            {2, 4, 3, 8, 1e-14}
        });
        panel.swap_rows(1, 2);
        snapshot(panel);
        panel.x[1][1].action(null, null);
        snapshot(panel);
        panel.x[3][1].action(null, null);
        snapshot(panel);
        panel.x[2][2].action(null, null);
        snapshot(panel);
        panel.x[1][2].action(null, null);
        snapshot(panel);
        System.exit(0);
    }
}
