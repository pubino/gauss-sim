package gauss;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import myutil.*;

public class GaussElim extends Frame
{
	TextField M = new TextField("3", 3);
	TextField N = new TextField("3", 3);
	int m = 3, n = 3;

	InPanel ip;
	Panel matrixPanel;

	public static void main(String[] args) {
		GaussElim frame = new GaussElim();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setVisible(true);
	}

	public GaussElim()
	{
		setTitle("Gaussian Elimination");
		setFont(new Font("Courier", Font.BOLD, 12));

		// Top panel with controls
		Panel p1 = new Panel(new FlowLayout(FlowLayout.LEFT));
		p1.setFont(new Font("Helvetica", Font.BOLD, 10));
		p1.add(new Label("Rows:"));
		p1.add(M);
		p1.add(new Label("Cols:"));
		p1.add(N);

		Button bResize = new Button("Resize");
		Button b1 = new Button("Generate Random Problem");
		Button b4 = new Button("Reset");
		Button b5 = new Button("Decimal");
		Button b6 = new Button("Fraction");

		p1.add(bResize);
		p1.add(b1);
		p1.add(b4);
		p1.add(b5);
		p1.add(b6);

		// Matrix panel
		matrixPanel = new Panel(new BorderLayout());
		ip = new InPanel(m, n);
		matrixPanel.add(ip, BorderLayout.CENTER);

		setLayout(new BorderLayout(10, 10));
		add(p1, BorderLayout.NORTH);
		add(matrixPanel, BorderLayout.CENTER);

		// Add action listeners
		bResize.addActionListener(e -> resizeMatrix());
		b1.addActionListener(e -> ip.genrand());
		b4.addActionListener(e -> ip.reset());
		b5.addActionListener(e -> {
			ip.setFmt("Decimal");
			ip.toGrid();
		});
		b6.addActionListener(e -> {
			ip.setFmt("Fraction");
			ip.toGrid();
		});

		setSize(700, 350);
	}

	void resizeMatrix()
	{
		int newM = Integer.valueOf(M.getText()).intValue();
		int newN = Integer.valueOf(N.getText()).intValue();
		if (newM > 0 && newN > 0 && (newM != m || newN != n)) {
			m = newM;
			n = newN;
			matrixPanel.removeAll();
			ip = new InPanel(m, n);
			matrixPanel.add(ip, BorderLayout.CENTER);
			matrixPanel.revalidate();
			matrixPanel.repaint();
			pack();
			setSize(Math.max(700, getWidth()), Math.max(350, getHeight()));
		}
	}
}

class InPanel extends GridPanel
{
	int m;
	int n;

	Color pink = new Color(255,0,255);

	double a[][];
	double b[];

	NumberField A[][];
	myButton x[][];

	myCheckbox c[];

	String xlabel[];

	NumberField B[];

        boolean fraction=false;

	public InPanel(int rows, int cols)
	{
		int i, j;

		m = rows;
		n = cols;

		xlabel = new String[n+1];

    		// for (j=1; j<=n; j++) { xlabel[j] = new String("x"+j); }
    		for (j=1; j<=n; j++) { xlabel[j] = "x"+j; }

		a = new double[m+1][n+1];
		b = new double[m+1];

		A = new NumberField[m+1][n+1];
		B = new NumberField[m+1];
		x = new myButton[m+1][n+1];
		c = new myCheckbox[m+1];

		Label plus[][]  = new Label[m+1][n+1];
		Label equals[] = new Label[m+1];

		Label pHeading = new Label("Current System");
		Label cHeading = new Label("Swap");

		for (i=1; i<=m; i++) {
		    for (j=1; j<=n; j++) {
			plus[i][j] = new Label("+");
		        x[i][j] = new myButton(xlabel[j],this,i,j);
			A[i][j] = new NumberField("0.0",9);
			if (i==j) { A[i][j].setBackground(pink); }
		    }
		    B[i] = new NumberField("0.0",9);
		    c[i] = new myCheckbox(this,i);
		    c[i].setState(false);
		}

		for (i=1; i<=m; i++) {
		    equals[i] = new Label("=");
		}


		add(cHeading,0,0,3,1);
		add(pHeading,3,0,3*n+2,1);

		for (i=1; i<=m; i++) {
			add(c[i], 0, i+2, 3, 1);
			for (j=1; j<=(n-1); j++) {
			    add(A[i][j],     3*j, i+2);
			    add(x[i][j],     3*j+1, i+2);
			    add(plus[i][j], 3*j+2,   i+2);

			}
			add(A[i][n],     3*n, i+2);
			add(x[i][n],     3*n+1, i+2);
			add(equals[i], 3*n+2, i+2);
			add(B[i], 3*(n+1), i+2);
		}
	}

	void diag(int row, double factor)
	{
		int i,j;
		double divide;

		divide = factor;

		for (i=1; i<=m; i++) {
			for (j=1; j<=n; j++) {
				a[i][j] = A[i][j].fromNumberField();
			}
			b[i] = B[i].fromNumberField();
		}



		if (divide != 0) {
			for (j=1; j<=n; j++) {
				a[row][j] /= divide;
			}
			b[row] /= divide;
		} else {
			return;
		}

		toGrid();

	}

	void nondiag(int row, int col, double factor)
	{
		int i,j;
		double multiple;
		double d[];
		double e;

		multiple = factor;

		for (i=1; i<=m; i++) {
			for (j=1; j<=n; j++) {
				a[i][j] = A[i][j].fromNumberField();
			}
			b[i] = B[i].fromNumberField();
		}


		d= new double[n+1];
		for (j=1; j<=n; j++) {
			d[j] = A[col][j].fromNumberField();
		}

		e= B[col].fromNumberField();

		for (j=1; j<=n; j++) {
			a[row][j] -= multiple*d[j];
		}
		b[row] -= multiple*e;

		toGrid();


	}

	public void swap_rows(int f, int s)
	{
		int i, j;
		int first, second;
		double d[];
		double e;

		first = f;
		second = s;

		for (i=1; i<=m; i++) {
			for (j=1; j<=n; j++) {
				a[i][j] = A[i][j].fromNumberField();
			}
			b[i] = B[i].fromNumberField();
		}

		d = new double[n+1];
		for (j=1; j<=n; j++) {
			d[j] = A[first][j].fromNumberField();
			a[first][j] = A[second][j].fromNumberField();
			a[second][j] = d[j];
		}

		e = B[first].fromNumberField();
		b[first] = B[second].fromNumberField();
		b[second] = e;

		toGrid();
	}


	public void genrand()
	{
		int i, j;

		double xinit[] = new double[n+1];
		double winit[] = new double[m+1];
		double yinit[] = new double[m+1];

		Random r = new Random();

		for (i=1; i<=m; i++) {
			for (j=1; j<=n; j++) {
			    if (r.nextDouble() < 0.5) {
			        a[i][j]  = (int) (5*r.nextDouble())-2;
			        a[i][j] += (int) (5*r.nextDouble())-2;
			        a[i][j] += (int) (5*r.nextDouble())-2;
			    } else {
				a[i][j] = 0;
			    }
			}
  		        b[i]  = (int) (5*r.nextDouble())-2;
		        b[i] += (int) (5*r.nextDouble())-2;
		        b[i] += (int) (5*r.nextDouble())-2;
		}

		if ( r.nextDouble() < 0.3 ) {

		} else {
		    for (j=1; j<=n; j++) {
			xinit[j] = (int) (3*r.nextDouble());
		    }

		    for (i=1; i<=m; i++) {
			winit[i] = (int) (3*r.nextDouble());
			yinit[i] = (int) (3*r.nextDouble());
		    }

		}


		toGrid();
		for (j=1; j<=n; j++) {
			for (i=1; i<=m; i++) {x[i][j].setLabel(xlabel[j]);}
		}

	}

	public void reset()
	{
		int i, j;

		for (i=1; i<=m; i++) {
			for (j=1; j<=n; j++) {
			    a[i][j] = 0;
			}
			b[i] = 0;
		}

		toGrid();

		for (j=1; j<=n; j++) {
			for (i=1; i<=m; i++) {x[i][j].setLabel(xlabel[j]);}
		}
	}

	public void toGrid()
	{
		int i, j;

		for (i=1; i<=m; i++) {
			for (j=1; j<=n; j++) {
			    A[i][j].toNumberField(a[i][j]);
			}
			B[i].toNumberField(b[i]);
			c[i].setState(false);
		}
	}

        void setFmt(String format)
        {
            // fmt.set(format);
	    int i, j;

	    for (i=1; i<=m; i++) {
		for (j=1; j<=n; j++) {
		    A[i][j].setFormat(format);
		}
		B[i].setFormat(format);
	    }
        }

	public boolean action(Event evt, Object arg)
	{
                int i,j;

                if (arg.equals("Rehighlight")) {
			for (i=1; i<=m; i++) {
				for (j=1; j<=n; j++) {
				    a[i][j] = A[i][j].fromNumberField();
				}
				b[i] = B[i].fromNumberField();
			}
                        return true;
                } else {
                        return super.action(evt,arg);
                }
	}
}

class myCheckbox extends Checkbox
{
	int row;
	InPanel ip;

	static int click=0, first, second;

	public myCheckbox(InPanel p, int r)
	{
		ip = p;
		row = r;
	}

	public boolean action(Event evt, Object arg)
	{
		if (getState() == true) {
			click++;
			if (click == 1) {
			    first = row;
			} else
			if (click == 2) {
			    click = 0;
			    second = row;
			    ip.swap_rows(first, second);
			    ip.c[first].setState(false);
			    ip.c[second].setState(false);
			}
		} else {
			click = 0;
		}
		return true;
	}
}

class myButton extends Button
{
	int row, col;
	InPanel ip;

	public myButton(String label, InPanel p, int r, int c)
	{
		super(label);

		row = r;
		col = c;
		ip  = p;
	}

	public boolean action(Event e, Object arg)
	{
		double f=0;

		if (row==col) {
			f = ip.A[row][col].fromNumberField();
			ip.diag(row,f);
		} else {
			if (col <= ip.m) {
			     	if (ip.A[col][col].fromNumberField() != 0) {
					f = ip.A[row][col].fromNumberField();
					f /= ip.A[col][col].fromNumberField();
					ip.nondiag(row,col,f);
				} else {
				}
			} else {
			}
		}

		return true;
	}
}
