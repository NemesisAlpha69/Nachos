package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2
 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) 
    {
    	this.conditionLock = conditionLock;//incializa la variable del lock, es un puntero
    	waitQueue=new LinkedList<KThread>();
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() 
    {
	   Lib.assertTrue(conditionLock.isHeldByCurrentThread());
        waitQueue.add(KThread.currentThread());
	   conditionLock.release();//cuando procede a hacer sleep entonces libera en lock antes
	
    	boolean intStatus=Machine.interrupt().disable();

	   KThread.sleep();
	   Machine.interrupt().restore(intStatus);
	   conditionLock.acquire();//para luego adquirir el lock de nuevo
    }//sleep()

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() 
    {
    	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
    	//if(!waitQueue.isEmpty())
     //   {
            boolean intStatus=Machine.interrupt().disable();
            waitQueue.removeFirst().ready();
            Machine.interrupt().restore(intStatus);
      //  }//if

    }//wake()

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() 
    {
    	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
    	while(!waitQueue.isEmpty())
    		wake();
    }//wakeAll
    
    private static class Condition2Test implements Runnable
    {
    	Condition2Test(Lock lock,Condition2 condition)
    	{
    		this.condition=condition;
    		this.lock=lock;
    	}//constructor
    	public void run()
    	{
    		lock.acquire();
    		//System.out.println(KThread.currentThread().getName() +" acuired lock");
    		condition.sleep();
    		//System.out.println(KThread.currentThread().getName()+ " acquired lock again ");
    		lock.release();
    		//System.out.println(KThread.currentThread().getName()+ " released lock");
    	}//run()
    	private Lock lock;
    	private Condition2 condition;
    }//Condition2Test
    
    public static void selfTest1()
    {
    	//System.out.println(" Enter Condition2.selfTest");
    	Lock lock=new Lock();
    	
    	Condition2 condition=new Condition2(lock);
    	
    	KThread t[]=new KThread[10];
    		for(int i=0;i<10;i++)
    		{
    			t[i]=new KThread(new Condition2Test(lock,condition));
    			t[i].setName("Thread" +i).fork();
    		} //for()
    	KThread.yield();
    	lock.acquire();
    	//System.out.println("condition.wake();");
    	condition.wake();
    	//System.out.println("condition.wakeAll();");
    	condition.wakeAll();
    	lock.release();
    	//System.out.println("Leave Condition2.selfTest");
    	t[9].join();
    	//System.out.println("t[9].join");
    }//selfTest()
    public static void selfTest2()
    {
        Lib.debug(dbgThread,"Entrando  a Condition2.selfTest()");
        //System.out.println("Entrando  a Condition2.selfTest()");
        Lock lock=new Lock();
        Condition2 c2=new Condition2(lock);
        //System.out.println("lock.acquire()");
        lock.acquire();
        
        //System.out.println("sleep()");
        c2.sleep();
        //System.out.println("wakeAll()");
    
        c2.wakeAll();
        c2.wake();
       // System.out.println("out wakeAll");
    }//selfTest2()
    
    //*************************************AQUI EMPIEZA SELFTEST PROVEIDO EN CLASE*********************************
    /**
	 * Tests whether this module is working.
	 */
	public static void selfTest3() {
		KThread parent = new KThread(new Runnable() {
			Communicator c = new Communicator();
			
			Speaker s = new Speaker(0xdeadbeef, c);
			Speaker s1 = new Speaker(0xFFFF, c);
			Speaker s2 = new Speaker(0x0000, c);
			
			
			
			Listener l = new Listener(c);
			Listener l1 = new Listener(c);
			Listener l2 = new Listener(c);
			
			
			KThread thread1 = new KThread(s).setName("Speaker Thread");
			KThread thread2 = new KThread(l).setName("Listener Thread");
			
			KThread thread3 = new KThread(s1).setName("Speaker Thread");
			KThread thread4 = new KThread(l1).setName("Listener Thread");
			
			KThread thread5 = new KThread(s2).setName("Speaker Thread");
			KThread thread6 = new KThread(l2).setName("Listener Thread");

			public void run() {
				thread1.fork();				
				thread2.fork();
				thread4.fork();
				thread6.fork();
				
				thread3.fork();
				thread5.fork();
				
				thread1.join();
				thread2.join();
				thread4.join();
				thread6.join();
				
				thread3.join();
				thread5.join();
				
				Lib.assertTrue(0xdeadbeef == l.getMessage());
			}
		});
		parent.fork();
		parent.join();
	}// seftTest();

	private static class Listener implements Runnable {
		private int msg;
		private Communicator commu;

		private Listener(Communicator commu) {
			this.commu = commu;
		}

		public void run() {
			msg = commu.listen();
		}

		private int getMessage() {
			return msg;
		}
	}

	private static class Speaker implements Runnable {
		private int msg;
		private Communicator commu;

		private Speaker(int msg, Communicator commu) {
			this.msg = msg;
			this.commu = commu;
		}

		public void run() {
			commu.speak(msg);
		}
	}

	/*
	 * 
	 * END selfTest PROVEIDO EN CLASE
	 */
    private Lock conditionLock;
    private LinkedList<KThread> waitQueue;
    private static final char dbgThread='t';
}//class Condition2
