using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Threading;

namespace Simulator
{
    public partial class Main : Form
    {
        private List<Pin> pins = new List<Pin>();
        Thread sketchProcess;

        public Main()
        {
            InitializeComponent();
        }

        private void Main_Load(object sender, EventArgs e)
        {
            for (int i = 0; i < 20; i++)
            {
                Pin pin = new Pin(i);
                pin.Size = new Size(pinsTable.Size.Width - 25, pin.Size.Height);
                pinsTable.Controls.Add(pin);
                pins.Add(pin);
                pin.Visible = false;
            }
            d11.Checked = d13.Checked = a0.Checked = true;
        }

        Random rnd = new Random();
        private void stepTimer_Tick(object sender, EventArgs e)
        {
            // HACK(Richo): To speed testing, I automatically change the value of A0
            Sketch.SetPinValue(14, Convert.ToInt16(Math.Sin((double)Environment.TickCount / 1000) * 1024));

            foreach (Pin pin in pins)
            {
                pin.UpdateValue();
            }
        }

        private void Main_FormClosing(object sender, FormClosingEventArgs e)
        {
            if (sketchProcess != null)
            {
                sketchProcess.Abort();
            }
        }

        private void Main_Resize(object sender, EventArgs e)
        {
            Point l = pinsTable.Location;
            pinsTable.Size = new Size(this.Size.Width, this.Size.Height - pinsTable.Location.Y);
            foreach (Control pin in pinsTable.Controls)
            {
                pin.Size = new Size(pinsTable.Size.Width - 25, pin.Size.Height);
            }
            pinsTable.Location = l;
        }

        private void connectBtn_Click(object sender, EventArgs e)
        {
            stepTimer.Enabled = !stepTimer.Enabled;
            connectBtn.Text = stepTimer.Enabled ?
                "Disconnect" :
                "Connect";
        }

        private void startButton_Click(object sender, EventArgs e)
        {
            if (sketchProcess == null)
            {
                sketchProcess = new Thread(Sketch.Start);
                sketchProcess.Start();
                startButton.Text = "Stop";
            }
            else
            {
                sketchProcess.Abort();
                sketchProcess = null;
                startButton.Text = "Start";
            }
        }

        private void toggle(Pin pin)
        {
            pin.Visible = !pin.Visible;
        }

        private void d0_CheckedChanged(object sender, EventArgs e)
        {
            toggle(pins[0]);
        }

        private void d1_CheckedChanged(object sender, EventArgs e)
        {
            toggle(pins[1]);
        }

        private void d2_CheckedChanged(object sender, EventArgs e)
        {
            toggle(pins[2]);
        }

        private void d3_CheckedChanged(object sender, EventArgs e)
        {
            toggle(pins[3]);
        }

        private void d4_CheckedChanged(object sender, EventArgs e)
        {
            toggle(pins[4]);
        }

        private void d5_CheckedChanged(object sender, EventArgs e)
        {
            toggle(pins[5]);
        }

        private void d6_CheckedChanged(object sender, EventArgs e)
        {
            toggle(pins[6]);
        }

        private void d7_CheckedChanged(object sender, EventArgs e)
        {
            toggle(pins[7]);
        }

        private void d8_CheckedChanged(object sender, EventArgs e)
        {
            toggle(pins[8]);
        }

        private void d9_CheckedChanged(object sender, EventArgs e)
        {
            toggle(pins[9]);
        }

        private void d10_CheckedChanged(object sender, EventArgs e)
        {
            toggle(pins[10]);
        }

        private void d11_CheckedChanged(object sender, EventArgs e)
        {
            toggle(pins[11]);
        }

        private void d12_CheckedChanged(object sender, EventArgs e)
        {
            toggle(pins[12]);
        }

        private void d13_CheckedChanged(object sender, EventArgs e)
        {
            toggle(pins[13]);
        }

        private void a0_CheckedChanged(object sender, EventArgs e)
        {
            toggle(pins[14]);
        }

        private void a1_CheckedChanged(object sender, EventArgs e)
        {
            toggle(pins[15]);
        }

        private void a2_CheckedChanged(object sender, EventArgs e)
        {
            toggle(pins[16]);
        }

        private void a3_CheckedChanged(object sender, EventArgs e)
        {
            toggle(pins[17]);
        }

        private void a4_CheckedChanged(object sender, EventArgs e)
        {
            toggle(pins[18]);
        }

        private void a5_CheckedChanged(object sender, EventArgs e)
        {
            toggle(pins[19]);
        }

        private void openSerialButton_Click(object sender, EventArgs e)
        {
            SerialConsole serial = new SerialConsole();
            serial.ShowDialog();
        }
    }
}
