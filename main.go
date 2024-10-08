package main

import (
	"log"
	"os"
	"runtime"
	"strconv"
	"sync"
)

// #cgo CFLAGS: -I${SRCDIR}/build/native/nativeCompile
// #cgo LDFLAGS: -L${SRCDIR}/build/native/nativeCompile -lnative
// #include <libnative.h>
//
// static graal_isolatethread_t* getOrAttachAndEnter(graal_isolate_t* isolate) {
//   graal_isolatethread_t *thread = graal_get_current_thread(isolate);
//   if (thread == NULL) {
//     if (graal_attach_thread(isolate, &thread) != 0) {
//       return NULL;
//     }
//   }
//  return thread;
// }
//
// static int increment_wrapper(graal_isolate_t* isolate) {
//   graal_isolatethread_t *thread = getOrAttachAndEnter(isolate);
//   if (thread == NULL) {
//     return -1;
//   }
//   return increment(thread);
// }
//
// static int get_wrapper(graal_isolate_t* isolate) {
//   graal_isolatethread_t *thread = getOrAttachAndEnter(isolate);
//   if (thread == NULL) {
//     return -1;
//   }
//   return get(thread);
// }
//
import "C"

var Pnr = new(runtime.Pinner)

func invoke(isolate *C.graal_isolate_t, wg *sync.WaitGroup) {
	defer wg.Done()
	C.increment_wrapper(isolate)
	return
}

func main() {
	log.SetFlags(log.Lshortfile)
	var isolate *C.graal_isolate_t
	var thread *C.graal_isolatethread_t
	Pnr.Pin(&isolate)
	Pnr.Pin(&thread)

	if C.graal_create_isolate(nil, &isolate, &thread) != 0 {
		log.Fatal("initialization error")
		return
	}

	count := 10
	if len(os.Args) > 1 {
		count, _ = strconv.Atoi(os.Args[1])
	}

	var wg sync.WaitGroup
	wg.Add(count)
	for i := 0; i < count; i++ {
		go invoke(isolate, &wg)
	}
	wg.Wait()

	result := C.get_wrapper(isolate)

	if result != C.int(count) {
		log.Fatalf("result(%d) does not match expected count(%d)\n", result, count)
	} else {
		log.Printf("result(%d) == expected count(%d)\n", result, count)
	}

	if C.graal_detach_all_threads_and_tear_down_isolate(thread) != 0 {
		log.Fatal("could not detach and tear down isolate successfully")
	}

	Pnr.Unpin()
	return
}
