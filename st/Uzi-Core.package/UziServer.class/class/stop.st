start/stop
stop
	default ifNotNil: [:server | server destroy]