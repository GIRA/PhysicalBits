building
buildButtons
	^ self newRow
		addMorphBack: self buildViewerButton;
		addMorphBack: (connectButton := self newButton
			label: 'Connect...';
			actionSelector: #toggleConnection;
			yourself);
		addMorphBack: (pinsButton := self newButton
			label: 'Pins';
			actionSelector: #togglePins;
			yourself);
		addMorphBack: (globalsButton := self newButton
			label: 'Globals';
			actionSelector: #toggleGlobals;
			yourself);
		addMorphBack: (editorButton := self newButton
			label: 'Editor';
			actionSelector: #openEditor;
			yourself);
		addMorphBack: (debuggerButton := self newButton
			label: 'Debugger...';
			actionSelector: #openDebugger;
			yourself);
		addMorphBack: (serverButton := self newButton
			label: 'Start server';
			actionSelector: #toggleServer;
			yourself);
		addMorphBack: self buildInspectorButton