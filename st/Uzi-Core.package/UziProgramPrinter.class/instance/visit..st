private
visit: anUziCompiledArtifact
	| index result |
	script ifNotNil: [
		index := (script instructions identityIndexOf: anUziCompiledArtifact).
		index > 0 ifTrue: [
			stream space; 
				nextPutAll: '"'; 
				print: index + (((program scripts copyUpTo: script)
					collect: [:each | each instructions size])
						ifEmpty: [0]
						ifNotEmpty: [:c | c sum]) - 1; 
				nextPutAll: '"']].
	result := anUziCompiledArtifact accept: self.
	^ result