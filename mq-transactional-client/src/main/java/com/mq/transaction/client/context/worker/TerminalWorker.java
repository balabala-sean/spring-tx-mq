package com.mq.transaction.client.context.worker;

/**
 * 线程的包装类
 *
 * @author ZhangYongjia
 */
public abstract class TerminalWorker implements Runnable {

    protected volatile boolean terminated = false;// 是否终止
    protected String tableName;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }

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
