building
buildSubmorphs
	^ self 
		addMorphBack: (continueButton := self newButton
			label: 'Continue';
			actionSelector: #continue;
			yourself);
		addMorphBack: (breakButton := self newButton
			label: 'Break';
			actionSelector: #break;
			yourself);
		addMorphBack: (overButton := self newButton
			label: 'Over';
			actionSelector: #stepOver;
			yourself)