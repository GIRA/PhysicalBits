namespace Simulator
{
    partial class SerialConsole
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            this.splitContainer1 = new System.Windows.Forms.SplitContainer();
            this.sendButton = new System.Windows.Forms.Button();
            this.inputTextBox = new System.Windows.Forms.TextBox();
            this.tabControl1 = new System.Windows.Forms.TabControl();
            this.asciiPage = new System.Windows.Forms.TabPage();
            this.asciiTextBox = new System.Windows.Forms.TextBox();
            this.hexPage = new System.Windows.Forms.TabPage();
            this.hexTextBox = new System.Windows.Forms.TextBox();
            this.updateTimer = new System.Windows.Forms.Timer(this.components);
            this.decPage = new System.Windows.Forms.TabPage();
            this.decTextBox = new System.Windows.Forms.TextBox();
            ((System.ComponentModel.ISupportInitialize)(this.splitContainer1)).BeginInit();
            this.splitContainer1.Panel1.SuspendLayout();
            this.splitContainer1.Panel2.SuspendLayout();
            this.splitContainer1.SuspendLayout();
            this.tabControl1.SuspendLayout();
            this.asciiPage.SuspendLayout();
            this.hexPage.SuspendLayout();
            this.decPage.SuspendLayout();
            this.SuspendLayout();
            // 
            // splitContainer1
            // 
            this.splitContainer1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.splitContainer1.Location = new System.Drawing.Point(0, 0);
            this.splitContainer1.Name = "splitContainer1";
            this.splitContainer1.Orientation = System.Windows.Forms.Orientation.Horizontal;
            // 
            // splitContainer1.Panel1
            // 
            this.splitContainer1.Panel1.Controls.Add(this.sendButton);
            this.splitContainer1.Panel1.Controls.Add(this.inputTextBox);
            // 
            // splitContainer1.Panel2
            // 
            this.splitContainer1.Panel2.Controls.Add(this.tabControl1);
            this.splitContainer1.Size = new System.Drawing.Size(575, 494);
            this.splitContainer1.SplitterDistance = 99;
            this.splitContainer1.TabIndex = 0;
            // 
            // sendButton
            // 
            this.sendButton.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.sendButton.Location = new System.Drawing.Point(466, 3);
            this.sendButton.Name = "sendButton";
            this.sendButton.Size = new System.Drawing.Size(106, 95);
            this.sendButton.TabIndex = 1;
            this.sendButton.Text = "Send";
            this.sendButton.UseVisualStyleBackColor = true;
            this.sendButton.Click += new System.EventHandler(this.sendButton_Click);
            // 
            // inputTextBox
            // 
            this.inputTextBox.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.inputTextBox.Font = new System.Drawing.Font("Consolas", 11.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.inputTextBox.Location = new System.Drawing.Point(3, 3);
            this.inputTextBox.Multiline = true;
            this.inputTextBox.Name = "inputTextBox";
            this.inputTextBox.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.inputTextBox.Size = new System.Drawing.Size(460, 95);
            this.inputTextBox.TabIndex = 0;
            // 
            // tabControl1
            // 
            this.tabControl1.Controls.Add(this.asciiPage);
            this.tabControl1.Controls.Add(this.hexPage);
            this.tabControl1.Controls.Add(this.decPage);
            this.tabControl1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tabControl1.Location = new System.Drawing.Point(0, 0);
            this.tabControl1.Margin = new System.Windows.Forms.Padding(0);
            this.tabControl1.Name = "tabControl1";
            this.tabControl1.SelectedIndex = 0;
            this.tabControl1.Size = new System.Drawing.Size(575, 391);
            this.tabControl1.TabIndex = 3;
            // 
            // asciiPage
            // 
            this.asciiPage.Controls.Add(this.asciiTextBox);
            this.asciiPage.Location = new System.Drawing.Point(4, 22);
            this.asciiPage.Name = "asciiPage";
            this.asciiPage.Padding = new System.Windows.Forms.Padding(3);
            this.asciiPage.Size = new System.Drawing.Size(567, 365);
            this.asciiPage.TabIndex = 0;
            this.asciiPage.Text = "ASCII";
            this.asciiPage.UseVisualStyleBackColor = true;
            // 
            // asciiTextBox
            // 
            this.asciiTextBox.Dock = System.Windows.Forms.DockStyle.Fill;
            this.asciiTextBox.Font = new System.Drawing.Font("Consolas", 11.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.asciiTextBox.Location = new System.Drawing.Point(3, 3);
            this.asciiTextBox.Multiline = true;
            this.asciiTextBox.Name = "asciiTextBox";
            this.asciiTextBox.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.asciiTextBox.Size = new System.Drawing.Size(561, 359);
            this.asciiTextBox.TabIndex = 2;
            // 
            // hexPage
            // 
            this.hexPage.Controls.Add(this.hexTextBox);
            this.hexPage.Location = new System.Drawing.Point(4, 22);
            this.hexPage.Name = "hexPage";
            this.hexPage.Padding = new System.Windows.Forms.Padding(3);
            this.hexPage.Size = new System.Drawing.Size(567, 365);
            this.hexPage.TabIndex = 1;
            this.hexPage.Text = "HEX";
            this.hexPage.UseVisualStyleBackColor = true;
            // 
            // hexTextBox
            // 
            this.hexTextBox.Dock = System.Windows.Forms.DockStyle.Fill;
            this.hexTextBox.Font = new System.Drawing.Font("Consolas", 11.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.hexTextBox.Location = new System.Drawing.Point(3, 3);
            this.hexTextBox.Multiline = true;
            this.hexTextBox.Name = "hexTextBox";
            this.hexTextBox.ScrollBars = System.Windows.Forms.ScrollBars.Both;
            this.hexTextBox.Size = new System.Drawing.Size(561, 359);
            this.hexTextBox.TabIndex = 3;
            this.hexTextBox.WordWrap = false;
            // 
            // updateTimer
            // 
            this.updateTimer.Interval = 1;
            this.updateTimer.Tick += new System.EventHandler(this.updateTimer_Tick);
            // 
            // decPage
            // 
            this.decPage.Controls.Add(this.decTextBox);
            this.decPage.Location = new System.Drawing.Point(4, 22);
            this.decPage.Name = "decPage";
            this.decPage.Padding = new System.Windows.Forms.Padding(3);
            this.decPage.Size = new System.Drawing.Size(567, 365);
            this.decPage.TabIndex = 2;
            this.decPage.Text = "DEC";
            this.decPage.UseVisualStyleBackColor = true;
            // 
            // decTextBox
            // 
            this.decTextBox.Dock = System.Windows.Forms.DockStyle.Fill;
            this.decTextBox.Font = new System.Drawing.Font("Consolas", 11.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.decTextBox.Location = new System.Drawing.Point(3, 3);
            this.decTextBox.Multiline = true;
            this.decTextBox.Name = "decTextBox";
            this.decTextBox.ScrollBars = System.Windows.Forms.ScrollBars.Both;
            this.decTextBox.Size = new System.Drawing.Size(561, 359);
            this.decTextBox.TabIndex = 4;
            this.decTextBox.WordWrap = false;
            // 
            // SerialConsole
            // 
            this.AcceptButton = this.sendButton;
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(575, 494);
            this.Controls.Add(this.splitContainer1);
            this.Name = "SerialConsole";
            this.Text = "Serial Console";
            this.Load += new System.EventHandler(this.SerialConsole_Load);
            this.splitContainer1.Panel1.ResumeLayout(false);
            this.splitContainer1.Panel1.PerformLayout();
            this.splitContainer1.Panel2.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.splitContainer1)).EndInit();
            this.splitContainer1.ResumeLayout(false);
            this.tabControl1.ResumeLayout(false);
            this.asciiPage.ResumeLayout(false);
            this.asciiPage.PerformLayout();
            this.hexPage.ResumeLayout(false);
            this.hexPage.PerformLayout();
            this.decPage.ResumeLayout(false);
            this.decPage.PerformLayout();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.SplitContainer splitContainer1;
        private System.Windows.Forms.TextBox inputTextBox;
        private System.Windows.Forms.Button sendButton;
        private System.Windows.Forms.Timer updateTimer;
        private System.Windows.Forms.TextBox asciiTextBox;
        private System.Windows.Forms.TabControl tabControl1;
        private System.Windows.Forms.TabPage asciiPage;
        private System.Windows.Forms.TabPage hexPage;
        private System.Windows.Forms.TextBox hexTextBox;
        private System.Windows.Forms.TabPage decPage;
        private System.Windows.Forms.TextBox decTextBox;
    }
}