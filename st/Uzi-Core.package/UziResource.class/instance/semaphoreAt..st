accessing
semaphoreAt: id
	^ sema at: id ifAbsentPut: Semaphore new