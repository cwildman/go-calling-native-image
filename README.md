# Go Calling A Native Image

This repository is meant to demonstrate that Go cannot work with a GraalVM native image
using the Native C API.

Go uses Goroutines for concurrency and Goroutines are not pinned to a single OS thread, instead they can:

1. Switch between OS threads as they execute
2. Yield and be preempted by a different Goroutine executing on the same OS thread

I suspect this creates at least two problems:

1. Two Goroutines can invoke the same entrypoint from the same OS thread before one of them has completed, which causes the VM to crash.
2. There is no guarantee that a goroutine has the correct isolate thread handle when invoking an entrypoint, because the OS thread may change by the time the entrypoint is invoked.

## Running The Example

1. Build the native image with the following:

```
./gradlew clean nativeCompile
```

2. Run the go program:

```
go run .
```

The default runs with only 10 goroutines and usually completes successfully.

3. Now try running with 1000 goroutines. If it doesn't fail right away try it a few times:

```
go run . 1000
```

Eventually you will see a `Fatal error: StackOverflowError:` with a large dump of debug data.

