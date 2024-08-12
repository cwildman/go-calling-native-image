# Go Calling A Native Image

This repository demonstrates how to call into a GraalVM native image from Go
using CGO and the Native C API.

Go's use of goroutines creates challenges when interacting with a native image because
a goroutine is not pinned to a single OS thread. Because goroutines can move between threads
they can create illegal states within the native image that result in a fatal error.

## The Solution

To avoid this problem one can build C wrapper functions within their CGO code
that fetch/attach the IsolateThread just before invoking an entry point. This guarantees
that a single OS thread is used for the entire duration of getting an IsolateThread and
invoking the entrypoint.

For example:

```
// #cgo LDFLAGS: -L${SRCDIR} -lnative
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
// static int my_entrypoint_wrapper(graal_isolate_t* isolate) {
//   graal_isolatethread_t *thread = getOrAttachAndEnter(isolate);
//   if (thread == NULL) {
//     return -1;
//   }
//   return my_entrypoint(thread);
// }
import "C"
```

Thank you to @christianhaeubl and @vjovanov for helping me figure this out.

## Running The Example

1. Build the native image with the following:

```
./gradlew clean nativeCompile
```

2. Run the go program:

```
go run .
```

The default runs with only 10 goroutines.

3. Now try running with 1000 or more goroutines. Previously this would fail, but it now works with the CGO wrapper to fetch the IsolateThread.

```
go run . 1000
```
