using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Data;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace Simulator
{
    public partial class Pin : UserControl
    {
        private int number = 0;
        private short value = 0;
        private string label;
        private Sketch sketch;

        public string Title { get { return label; } }
        public int Number { get { return number; } }
        public bool IsDigital { get { return number < 14; } }

        public Pin() : this(0, Sketch.Current) { }

        public Pin(int number, Sketch sketch)
        {
            this.number = number;
            this.sketch = sketch;
            label = string.Format("{0}{1}",
                IsDigital ? "D" : "A",
                IsDigital ? number : number - 14);            

            InitializeComponent();
        }

        private void Pin_Load(object sender, EventArgs e)
        {
            title.Text = label;
        }

        private void numericUpDown1_ValueChanged(object sender, EventArgs e)
        {
            sketch.SetPinValue(number, Convert.ToInt16(numericUpDown1.Value));
        }

        public void UpdateValue()
        {
            value = sketch.GetPinValue(number);
            numericUpDown1.Value = value;
            graph.Add(value);
            led.Image = value > 0 ?
                Properties.Resources.on :
                Properties.Resources.off;
        }

        private void led_Click(object sender, EventArgs e)
        {
            sketch.SetPinValue(number, (short)(1023 - value));
        }
    }
}
