streaming
streamFrom: srcStream to: dstStream size: sizeOrNil progress: progressBlock
	"Stream the content of srcStream to dstStream.
	If a size is given, stream that many elements, otherwise stream up to the end."

	| buffer totalRead remaining size |
	(self headerAt: 'transfer-encoding') ifNotEmpty:[:encoding|
		encoding = 'chunked' 
			ifTrue:[^self chunkFrom: srcStream to: dstStream progress: progressBlock]
			ifFalse:[self error: 'Unknown transfer-encoding: ', encoding]].

	sizeOrNil = 0 ifTrue:[^self].

	buffer := (srcStream isBinary ifTrue:[ByteArray] ifFalse:[String]) new: 4096.
	totalRead := 0.
	size := sizeOrNil ifNil:[0].
	[(sizeOrNil == nil and:[stream atEnd not]) or:[totalRead < size]] whileTrue:[
		progressBlock ifNotNil:[progressBlock value: sizeOrNil value: totalRead].
		remaining := sizeOrNil ifNil:[99999] ifNotNil:[sizeOrNil - totalRead].
		remaining > buffer size ifTrue:[remaining := buffer size].
		buffer := srcStream next: remaining into: buffer startingAt: 1.
		dstStream nextPutAll: (remaining < buffer size  
			ifTrue:[(buffer copyFrom: 1 to: remaining)]
			ifFalse:[buffer]).
		totalRead := totalRead + buffer size.
	].
	dstStream flush.
	progressBlock ifNotNil:[progressBlock value: sizeOrNil value: totalRead].