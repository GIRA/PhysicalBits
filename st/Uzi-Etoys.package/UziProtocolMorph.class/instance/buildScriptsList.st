building
buildScriptsList
	| scriptsList scripts |
	scriptsList := self newColumn
		cellPositioning: #topLeft;
		cellInset: 5.
	scripts := self scripts.
	(scriptTexts keys copyWithoutAll: scripts)
		do: [:key |
			(scriptTexts at: key) delete.
			scriptTexts removeKey: key].
	scripts
		ifEmpty: [scriptsList addMorphBack: '  *no scripts found*' asMorph]
		ifNotEmpty: [scripts
			do: [:script || m |
				m := scriptTexts at: script
					ifAbsentPut: ['' asMorph].
				scriptsList addMorphBack: m]].
	^ scriptsList