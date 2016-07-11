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
        private int _number = 0;
        private int _value = 0;

        public int Number
        {
            get
            {
                return _number;
            }
            set
            {
                _number = value;
            }
        }

        public Pin(int n)
        {
            _number = n;
            InitializeComponent();
        }

        private void Pin_Load(object sender, EventArgs e)
        {
        }

        private void Pin_Paint(object sender, PaintEventArgs e)
        {
            label1.Text = _number.ToString();
        }

        private void numericUpDown1_ValueChanged(object sender, EventArgs e)
        {
            Sketch.SetPinValue(_number, Convert.ToInt16(numericUpDown1.Value));
        }

        public void UpdateValue()
        {
            _value = Sketch.GetPinValue(_number);
            numericUpDown1.Value = _value;
            graph.Add(_value);
            led.Image = _value > 0 ?
                Properties.Resources.on :
                Properties.Resources.off;
        }

        private void led_Click(object sender, EventArgs e)
        {
            Sketch.SetPinValue(_number, Convert.ToInt16(_value > 0 ? 0 : 1023));
        }
        
    }
}
