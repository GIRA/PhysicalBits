using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Threading;
using System.Threading.Tasks;

namespace Simulator
{
    public partial class Main : Form
    {
        private const int PIN_COUNT = 20;
        private Pin[] pins = new Pin[PIN_COUNT];
        private CheckBox[] checks = new CheckBox[PIN_COUNT];
        private SerialConsole serial;

        private Sketch sketch = Sketch.Current;

        public Main()
        {
            InitializeComponent();
        }

        private void Main_Load(object sender, EventArgs e)
        {
            InitializePins();
            stepTimer.Enabled = true;
            sketch.Start();
            // HACK(Richo): To quickly test, I run a default program
            Task.Delay(500) // Wait a bit to give time to the Serial to initialize
                .ContinueWith(task =>
                {
                    sketch.WriteSerial(new byte[]
                    {
                        0, // Run program

                        2, // Script count

                        // Globals
                        5, // Total # of globals
                        20, // 5 variables of 1 byte each
                        0, 1, 11, 13, 15, // Initial values

                        // Script 1: #Blink13 (with IF)
                        128, 0, 3, 232, // Header: ticking 1/s
                        11, // Bytecode count
                        0x83,       // PUSH 13
                        0x83,       // PUSH 13
                        0xA0,       // PRIM #read
                        0xF2, 0x03, // JNZ  3
                        0x81,       // PUSH 1
                        0xA1,       // PRIM #write
                        0xFF, 0x02, // JMP  2
                        0x80,       // PUSH 0
                        0xA1,       // PRIM #write
                        
                        // Script 2: #Pot15Led11
                        128, 0, 0, 0, // Header: ticking
                        4, // Bytecode count
                        0x82,       // PUSH 11
                        0x84,       // PUSH 15
                        0xA0,       // PRIM #read
                        0xA1,       // PRIM #write
                    });
                });
                 
        }

        private void InitializePins()
        {
            pinsTable.Padding = new Padding(0, 0, SystemInformation.VerticalScrollBarWidth, 0);
            for (int i = 0; i < PIN_COUNT; i++)
            {
                Pin pin = new Pin(i, sketch);
                pinsTable.Controls.Add(pin);
                pin.Anchor = AnchorStyles.Left | AnchorStyles.Right;
                pin.Visible = false;

                CheckBox check = new CheckBox();
                check.Text = pin.Title;
                checksTable.Controls.Add(check);

                pins[i] = pin;
                checks[i] = check;
            }

            // HACK(Richo): By default I make these three pins visible
            checks[11].Checked = checks[13].Checked = checks[15].Checked = true;
        }


        private void stepTimer_Tick(object sender, EventArgs e)
        {
            // HACK(Richo): To speed testing, I automatically change the value of A0
            sketch.SetPinValue(15, Convert.ToInt16(Math.Sin((double)Environment.TickCount / 1000) * 1024));
            
            UpdateUI();
        }

        private void UpdateUI()
        {
            startButton.Enabled = !sketch.Running;
            pauseButton.Enabled = sketch.Running;
            stopButton.Enabled = sketch.Running;
            openSerialButton.Enabled = serial == null || serial.IsDisposed;

            for (int i = 0; i < PIN_COUNT; i++)
            {
                if (sketch.Running) { pins[i].UpdateValue(); }
                pins[i].Visible = checks[i].Checked;
            }

            string state = "Unknown";
            if (sketch.Running) { state = "Running"; }
            else if (sketch.Paused) { state = "Paused"; }
            else if (sketch.Stopped) { state = "Stopped"; }
            Text = string.Format("Arduino Simulator [{0}]", state);
        }

        private void Main_FormClosing(object sender, FormClosingEventArgs e)
        {
            sketch.Stop();
        }

        private void startButton_Click(object sender, EventArgs e)
        {
            sketch.Start();
        }

        private void toggle(Pin pin)
        {
            pin.Visible = !pin.Visible;
        }

        private void openSerialButton_Click(object sender, EventArgs e)
        {
            // Only one serial console open
            if (serial != null && !serial.IsDisposed) return;

            serial = new SerialConsole(sketch);
            serial.Show();
        }

        private void stopButton_Click(object sender, EventArgs e)
        {
            sketch.Stop();
        }

        private void pauseButton_Click(object sender, EventArgs e)
        {
            sketch.Pause();
        }

    }
}
