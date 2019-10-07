package apptentive.com.android.core

import android.os.Looper
import apptentive.com.android.concurrent.ConcurrentExecutorQueue
import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.concurrent.SerialExecutorQueue

// TODO: rename to ExecutorFactory
interface ExecutorQueueFactory {
    fun createMainQueue(): ExecutorQueue
    fun createSerialQueue(name: String): ExecutorQueue
    fun createConcurrentQueue(name: String, maxConcurrentTasks: Int? = null): ExecutorQueue
}

// TODO: rename to AndroidExecutorFactoryProvider
class DefaultExecutorQueueFactoryProvider : Provider<ExecutorQueueFactory> {
    private val factory: ExecutorQueueFactory by lazy { DefaultExecutorQueueFactory() }
    override fun get(): ExecutorQueueFactory = factory
}

// TODO: rename to AndroidExecutorFactory
private class DefaultExecutorQueueFactory : ExecutorQueueFactory {
    override fun createMainQueue(): ExecutorQueue {
        return SerialExecutorQueue(Looper.getMainLooper(), "main")
    }

    override fun createSerialQueue(name: String): ExecutorQueue {
        return SerialExecutorQueue(name)
    }

    override fun createConcurrentQueue(name: String, maxConcurrentTasks: Int?): ExecutorQueue {
        return ConcurrentExecutorQueue(name, maxConcurrentTasks)
    }
}