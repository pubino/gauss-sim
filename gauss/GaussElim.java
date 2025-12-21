package gauss;

import java.awt.*;
import java.applet.*;
import java.awt.image.*;
import java.util.*;
import java.lang.String;

import myutil.*;

public class GaussElim extends Applet
{
	PivotFrame pf;
	TextField M = new TextField("3");
	TextField N = new TextField("3");
	int m, n;

        public static void main(String[] args) {
                new AppletFrame(new GaussElim(), 420, 90);
        }

        public void init()
	{
                setLayout(new FlowLayout(FlowLayout.LEFT));
                setFont(new Font("Courier",Font.BOLD,12));
                add(new Label("rows"));
                add(M);
                add(new Label("cols"));
                add(N);
                add(new Button("Go Eliminating"));
        }

        public boolean action(Event evt, Object arg)
        {
                if (arg.equals("Go Eliminating")) {
                        m = Integer.valueOf(M.getText()).intValue();
                        n = Integer.valueOf(N.getText()).intValue();
                        pf = new PivotFrame(m,n);
                        pf.show();
                } else {
                        return super.action(evt, arg);
                }
                return true;
        }

        public boolean handleEvent(Event evt)
        {
                if (evt.id == Event.WINDOW_DESTROY) System.exit(0);
                return super.handleEvent(evt);
        }
}

class PivotFrame extends Frame
{
	int m, n;

	InPanel   ip;

        Choice fmt;

	public PivotFrame(int rows, int cols)
	{
		m = rows;
		n = cols;

		ip = new InPanel(m,n);

		Button b1 = new Button("Generate Random Problem");
		Button b4 = new Button("Reset");
		Button b3 = new Button("Exit");
		Button b5 = new Button("Decimal");
		Button b6 = new Button("Fraction");

		Panel p1 = new Panel();
		p1.setFont(new Font("Helvetica",Font.BOLD,10));
		p1.add(b1);
		p1.add(b4);
		p1.add(b3);
                p1.add(b5);
                p1.add(b6);

		setLayout(new BorderLayout(10,10));
		add("North",p1);
		add("Center",ip);

		resize(500, 300);
	}

	public boolean action(Event evt, Object arg)
	{
		int i, j;

		if (arg.equals("Exit")) {
			dispose();
		}
		else if (arg.equals("Reset")) {
			ip.reset();
		}
		else if (arg.equals("Generate Random Problem")) {
			ip.genrand();
		}
		else if (arg.equals("Decimal")) {
			ip.setFmt("Decimal");
                        ip.toGrid();
		}
		else if (arg.equals("Fraction")) {
			ip.setFmt("Fraction");
                        ip.toGrid();
		}
		else return super.action(evt,arg);
		return true;
	}

	public boolean handleEvent(Event evt)
	{
		return super.handleEvent(evt);
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
