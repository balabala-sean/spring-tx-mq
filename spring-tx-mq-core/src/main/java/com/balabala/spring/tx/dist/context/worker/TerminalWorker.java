package com.balabala.spring.tx.dist.context.worker;

/**
 * 线程的包装类
 *
 * @author ZhangYongjia
 */
public abstract class TerminalWorker implements Runnable {

    protected volatile boolean terminated = false;// 是否终止

    public void terminate(){
        this.terminated = true;
    }

    public boolean isTerminated(){
        return this.terminated;
    }

    @Override
    public void run() {
        prepare();

        loop : while(true){
            this.work();
            if (terminated) break loop;
        }

        after();
    }

    protected abstract void prepare();
    protected abstract void work();
    protected abstract void after();
}
