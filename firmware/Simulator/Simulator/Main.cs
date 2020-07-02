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
using System.IO;

namespace Simulator
{
    public partial class Main : Form
    {
        private const string EEPROM_FILE = "eeprom.mem";
        private const int PIN_COUNT = 20;
        private Pin[] pins = new Pin[PIN_COUNT];
        private CheckBox[] checks = new CheckBox[PIN_COUNT];
        private SerialConsole serial;
        private SocketConnection socket;

        private Sketch sketch = Sketch.Current;

        public Main()
        {
            InitializeComponent();
        }

        private void Main_Load(object sender, EventArgs e)
        {
            InitializeSocket();
            InitializePins();
            ReadEEPROMFile();
            stepTimer.Enabled = true;
            sketch.Start();
        }

        private void InitializeSocket()
        {
            socket = new SocketConnection(sketch);
            socket.Start();
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

        private void ReadEEPROMFile()
        {
            if (File.Exists(EEPROM_FILE))
            {
                try
                {
                    sketch.WriteEEPROM(File.ReadAllBytes(EEPROM_FILE));
                }
                catch (Exception ex)
                {
                    // INFO(Richo): The file could not exist, or we could not have access to it
                    MessageBox.Show(ex.ToString());
                }
            }
        }

        private void WriteEEPROMFile()
        {
            try
            {
                File.WriteAllBytes(EEPROM_FILE, sketch.ReadEEPROM());
            }
            catch (Exception ex)
            {
                // INFO(Richo): We could not have write access to the file or something
                MessageBox.Show(ex.ToString());
            }
        }

        private void stepTimer_Tick(object sender, EventArgs e)
        {
            // HACK(Richo): To speed testing, I automatically change the value of A0
            //sketch.SetPinValue(15, Convert.ToInt16(Math.Sin((double)Environment.TickCount / 1000) * 1024));

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

            string sketchState = "Unknown";
            if (sketch.Running) { sketchState = "Running"; }
            else if (sketch.Paused) { sketchState = "Paused"; }
            else if (sketch.Stopped) { sketchState = "Stopped"; }

            string socketState = "Unknown";
            if (socket.Connected) { socketState = "Connected"; }
            else if (socket.Running) { socketState = string.Format("Listening on port {0}", socket.Port); }

            Text = string.Format("Arduino Simulator [Sketch: {0}][Socket: {1}]", sketchState, socketState);
        }

        private void Main_FormClosing(object sender, FormClosingEventArgs e)
        {
            sketch.Stop();
            socket.Stop();
            WriteEEPROMFile();
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
