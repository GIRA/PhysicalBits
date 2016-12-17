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
		addMorphBack: self buildInspectorButton