namespace Simulator
{
    partial class Main
    {
        /// <summary>
        /// Variable del diseñador requerida.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Limpiar los recursos que se estén utilizando.
        /// </summary>
        /// <param name="disposing">true si los recursos administrados se deben eliminar; false en caso contrario, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Código generado por el Diseñador de Windows Forms

        /// <summary>
        /// Método necesario para admitir el Diseñador. No se puede modificar
        /// el contenido del método con el editor de código.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            this.stepTimer = new System.Windows.Forms.Timer(this.components);
            this.pinsTable = new System.Windows.Forms.TableLayoutPanel();
            this.startButton = new System.Windows.Forms.Button();
            this.digitalPinsLabel = new System.Windows.Forms.Label();
            this.openSerialButton = new System.Windows.Forms.Button();
            this.stopButton = new System.Windows.Forms.Button();
            this.digitalChecksTable = new System.Windows.Forms.TableLayoutPanel();
            this.analogChecksTable = new System.Windows.Forms.TableLayoutPanel();
            this.analogPinsLabel = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // stepTimer
            // 
            this.stepTimer.Interval = 1;
            this.stepTimer.Tick += new System.EventHandler(this.stepTimer_Tick);
            // 
            // pinsTable
            // 
            this.pinsTable.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.pinsTable.AutoScroll = true;
            this.pinsTable.BackColor = System.Drawing.SystemColors.ControlDark;
            this.pinsTable.ColumnCount = 1;
            this.pinsTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.pinsTable.GrowStyle = System.Windows.Forms.TableLayoutPanelGrowStyle.FixedSize;
            this.pinsTable.Location = new System.Drawing.Point(12, 109);
            this.pinsTable.Name = "pinsTable";
            this.pinsTable.RowCount = 20;
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.pinsTable.Size = new System.Drawing.Size(805, 488);
            this.pinsTable.TabIndex = 6;
            // 
            // startButton
            // 
            this.startButton.Location = new System.Drawing.Point(12, 12);
            this.startButton.Name = "startButton";
            this.startButton.Size = new System.Drawing.Size(123, 33);
            this.startButton.TabIndex = 8;
            this.startButton.Text = "Start";
            this.startButton.UseVisualStyleBackColor = true;
            this.startButton.Click += new System.EventHandler(this.startButton_Click);
            // 
            // digitalPinsLabel
            // 
            this.digitalPinsLabel.Location = new System.Drawing.Point(12, 51);
            this.digitalPinsLabel.Name = "digitalPinsLabel";
            this.digitalPinsLabel.Size = new System.Drawing.Size(76, 23);
            this.digitalPinsLabel.TabIndex = 10;
            this.digitalPinsLabel.Text = "Digital pins:";
            this.digitalPinsLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // openSerialButton
            // 
            this.openSerialButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.openSerialButton.Location = new System.Drawing.Point(695, 12);
            this.openSerialButton.Name = "openSerialButton";
            this.openSerialButton.Size = new System.Drawing.Size(123, 33);
            this.openSerialButton.TabIndex = 46;
            this.openSerialButton.Text = "Open Serial";
            this.openSerialButton.UseVisualStyleBackColor = true;
            this.openSerialButton.Click += new System.EventHandler(this.openSerialButton_Click);
            // 
            // stopButton
            // 
            this.stopButton.Location = new System.Drawing.Point(141, 12);
            this.stopButton.Name = "stopButton";
            this.stopButton.Size = new System.Drawing.Size(123, 33);
            this.stopButton.TabIndex = 47;
            this.stopButton.Text = "Stop";
            this.stopButton.UseVisualStyleBackColor = true;
            this.stopButton.Click += new System.EventHandler(this.stopButton_Click);
            // 
            // digitalChecksTable
            // 
            this.digitalChecksTable.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.digitalChecksTable.ColumnCount = 14;
            this.digitalChecksTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.digitalChecksTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.digitalChecksTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.digitalChecksTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.digitalChecksTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.digitalChecksTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.digitalChecksTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.digitalChecksTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.digitalChecksTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.digitalChecksTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.digitalChecksTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.digitalChecksTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.digitalChecksTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.digitalChecksTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 73F));
            this.digitalChecksTable.GrowStyle = System.Windows.Forms.TableLayoutPanelGrowStyle.AddColumns;
            this.digitalChecksTable.Location = new System.Drawing.Point(94, 51);
            this.digitalChecksTable.Name = "digitalChecksTable";
            this.digitalChecksTable.RowCount = 1;
            this.digitalChecksTable.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.digitalChecksTable.Size = new System.Drawing.Size(723, 23);
            this.digitalChecksTable.TabIndex = 48;
            // 
            // analogChecksTable
            // 
            this.analogChecksTable.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.analogChecksTable.ColumnCount = 6;
            this.analogChecksTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.analogChecksTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.analogChecksTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.analogChecksTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.analogChecksTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.analogChecksTable.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 473F));
            this.analogChecksTable.Location = new System.Drawing.Point(94, 80);
            this.analogChecksTable.Name = "analogChecksTable";
            this.analogChecksTable.RowCount = 1;
            this.analogChecksTable.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.analogChecksTable.Size = new System.Drawing.Size(723, 23);
            this.analogChecksTable.TabIndex = 50;
            // 
            // analogPinsLabel
            // 
            this.analogPinsLabel.Location = new System.Drawing.Point(12, 80);
            this.analogPinsLabel.Name = "analogPinsLabel";
            this.analogPinsLabel.Size = new System.Drawing.Size(76, 23);
            this.analogPinsLabel.TabIndex = 49;
            this.analogPinsLabel.Text = "Analog pins:";
            this.analogPinsLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // Main
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(830, 609);
            this.Controls.Add(this.pinsTable);
            this.Controls.Add(this.analogChecksTable);
            this.Controls.Add(this.analogPinsLabel);
            this.Controls.Add(this.digitalChecksTable);
            this.Controls.Add(this.stopButton);
            this.Controls.Add(this.openSerialButton);
            this.Controls.Add(this.digitalPinsLabel);
            this.Controls.Add(this.startButton);
            this.Name = "Main";
            this.Text = "Arduino Simulator";
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.Main_FormClosing);
            this.Load += new System.EventHandler(this.Main_Load);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Timer stepTimer;
        private System.Windows.Forms.TableLayoutPanel pinsTable;
        private System.Windows.Forms.Button startButton;
        private System.Windows.Forms.Label digitalPinsLabel;
        private System.Windows.Forms.Button openSerialButton;
        private System.Windows.Forms.Button stopButton;
        private System.Windows.Forms.TableLayoutPanel digitalChecksTable;
        private System.Windows.Forms.TableLayoutPanel analogChecksTable;
        private System.Windows.Forms.Label analogPinsLabel;
    }
}

