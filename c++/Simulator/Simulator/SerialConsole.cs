using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace Simulator
{
    public partial class SerialConsole : Form
    {
        public SerialConsole()
        {
            InitializeComponent();
        }

        private void SerialConsole_Load(object sender, EventArgs e)
        {
            updateTimer.Enabled = true;
        }

        private void sendButton_Click(object sender, EventArgs e)
        {
            if (string.IsNullOrWhiteSpace(inputTextBox.Text)) return;
            outputTextBox.AppendText(string.Format("{0:HH:mm:ss.fff}| {1}", DateTime.Now, inputTextBox.Text));
            outputTextBox.AppendText(Environment.NewLine);
            inputTextBox.Clear();
        }

        private void updateTimer_Tick(object sender, EventArgs e)
        {
        }
    }
}
