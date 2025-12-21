package myutil;

import java.awt.*;

public class NumberField extends TextField
{
        public double x;

	Format fmt;

        public NumberField(String label, int width)
        {
                super(label, width);
                x = Double.valueOf(label).doubleValue();
		fmt = new Format(width,width/2);
        }

        public NumberField(String label, int width, int prec)
        {
                super(label, width);
                x = Double.valueOf(label).doubleValue();
		fmt = new Format(width,prec);
        }

        public double toNumberField(double t)
        {
                if (t < 1.0e-12 && t > -1.0e-12) { t = 0; }
                x = t;

                setText( fmt.form(t) );

                return t;
        }

        public double fromNumberField()
        {
                return x;
        }

	public void setFormat(String format)
	{
		fmt.set(format);
	}

	public void setWidth(int w)
	{
		fmt.setWidth(w);
	}

	public void setPrecision(int p)
	{
		fmt.setPrecision(p);
	}

        public boolean keyDown(Event e, int k)
	{
		if (k==10) {
			e.key = 9;
			deliverEvent(e);
		}
                return super.keyDown(e,k);
	}

        public boolean lostFocus(Event e, Object w)
        {
		int i, n;
		double num, den;
		String s = new String(getText());

		n = s.length();
		i = s.indexOf('/');
		if (n == 0) {
			x = 0;
		} else
		if (i != -1) {
			num = Double.valueOf(s.substring(0,i)).doubleValue();
			den = Double.valueOf(s.substring(i+1,n)).doubleValue();
			x = num/den;
		} else {
                	x = Double.valueOf(s).doubleValue();
		}

		String text = "Rehighlight";
		Event evt = new Event(this, Event.ACTION_EVENT, (Object)text);
		deliverEvent(evt);

                return super.lostFocus(e,w);
        }

        public boolean gotFocus(Event e, Object w)
        {
		selectAll();
                return super.gotFocus(e,w);
        }
}
