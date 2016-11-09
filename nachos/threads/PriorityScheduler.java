package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;


import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A scheduler that chooses threads based on their priorities.
 *	un calendarizador que selecciona threads en base a su prioridad.
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *	Un priority scheduler asocia una prioridad con cada thread. 
 * el siguiente thread que sea dequeued sera siempre un thread con prioridad no menor que 
 *a cualquier otro thread en espera. como un round-robin scheduler, el thread que
 *es dequeued es, entre todos los threads de la misma (mas alta) prioridad, el 
 *thread que ha estado esperando mas tiempo.           
	 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 *basicamente, un priority scheduler da acceso en una modalidad de round robin 
 *a todos los threads de alta prioridad, e ignora a los demas.     
 *esto puede ser potencialmente un estorbo a un thread si este siempre esta un thread de alta prioridad esperando.  
 *                     
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 *
 *	Un priority scheduler debe resolver parcialmente el priority inversion; 
 *en particular, la prioridad debe ser donada mediante locks, y mediante joins.
 *
 */
public class PriorityScheduler extends Scheduler 
{
    /**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() 
    {
    
    }
	/**********************************************************************
	 * Tests whether this module is working.
	 */

	public static void selfTestRun(KThread t1, int t1p, KThread t2, int t2p) {
		boolean int_state;
		t1.setName("a");
		t2.setName("b");

		int_state = Machine.interrupt().disable();
		ThreadedKernel.scheduler.setPriority(t1, t1p);
		ThreadedKernel.scheduler.setPriority(t2, t2p);
		Machine.interrupt().restore(int_state);
		t1.fork();
		t2.fork();

		t1.join();
		t2.join();
	}

	public static void selfTestRun(KThread t1, int t1p, KThread t2, int t2p,KThread t3, int t3p) 
	{
		boolean int_state;
		t1.setName("a");
		t2.setName("b");
		t3.setName("c");

		int_state = Machine.interrupt().disable();
		ThreadedKernel.scheduler.setPriority(t1, t1p);
		ThreadedKernel.scheduler.setPriority(t2, t2p);
		ThreadedKernel.scheduler.setPriority(t3, t3p);
		Machine.interrupt().restore(int_state);

		t1.fork();
		t2.fork();
		t3.fork();

		t1.join();
		t2.join();
		t3.join();
	}//selfTestRun

	/**
	 * Tests whether this module is working.
	 */
	//public static void selfTest() {}
	public static void selfTest1() 
	{
		KThread t1, t2, t3;
		final Lock lock;
		@SuppressWarnings("unused")
		final Condition2 condition;
		/*
		 * Case 1: Tests priority scheduler without donation
		 * 
		 * This runs t1 with priority 7, and t2 with priority 4.
		 */
		System.out.println("\n\n\nCase 1:");
		t1 = new KThread(new Runnable() {
			public void run() {
				System.out.println(KThread.currentThread().getName()
						+ " started working");
				for (int i = 0; i < 10; ++i) {
					boolean ee = Machine.interrupt().disable();

					System.out.println(KThread.currentThread().getName()
							+ " working "
							+ i
							+ " P: "
							+ ThreadedKernel.scheduler.getPriority(KThread
									.currentThread())
							+ " Ef: "
							+ ThreadedKernel.scheduler
									.getEffectivePriority((KThread
											.currentThread())));
					Machine.interrupt().restore(ee);

					KThread.yield();
				}
				System.out.println(KThread.currentThread().getName()
						+ " finished working");
			}
		});
		t2 = new KThread(new Runnable() {
			public void run() {
				System.out.println(KThread.currentThread().getName()
						+ " started working");
				for (int i = 0; i < 10; ++i) {
					boolean ee = Machine.interrupt().disable();

					System.out.println(KThread.currentThread().getName()
							+ " working "
							+ i
							+ " P: "
							+ ThreadedKernel.scheduler.getPriority(KThread
									.currentThread())
							+ " Ef: "
							+ ThreadedKernel.scheduler
									.getEffectivePriority((KThread
											.currentThread())));
					Machine.interrupt().restore(ee);

					KThread.yield();
				}
				System.out.println(KThread.currentThread().getName()
						+ " finished working");
			}
		});
		selfTestRun(t1, 7, t2, 4);

		/*
		 * Case 2: Tests priority scheduler without donation, altering
		 * priorities of threads after they've started running
		 * 
		 * This runs t1 with priority 7, and t2 with priority 4, but half-way
		 * through t1's process its priority is lowered to 2.
		 */

		System.out.println("\n\n\nCase 2:");
		t1 = new KThread(new Runnable() {
			public void run() {
				System.out.println(KThread.currentThread().getName()
						+ " started working");
				for (int i = 0; i < 10; ++i) {
					boolean ee = Machine.interrupt().disable();
					System.out.println(KThread.currentThread().getName()
							+ " working "
							+ i
							+ " P: "
							+ ThreadedKernel.scheduler.getPriority(KThread
									.currentThread())
							+ " Ef: "
							+ ThreadedKernel.scheduler
									.getEffectivePriority((KThread
											.currentThread())));
					Machine.interrupt().restore(ee);
					KThread.yield();
					if (i == 4) {
						System.out.println(KThread.currentThread().getName()
								+ " reached 1/2 way, changing priority");
						boolean int_state = Machine.interrupt().disable();
						ThreadedKernel.scheduler.setPriority(2);
						Machine.interrupt().restore(int_state);
					}
				}
				System.out.println(KThread.currentThread().getName()
						+ " finished working");
			}
		});
		t2 = new KThread(new Runnable() {
			public void run() {
				System.out.println(KThread.currentThread().getName()
						+ " started working");
				for (int i = 0; i < 10; ++i) {
					boolean ee = Machine.interrupt().disable();
					System.out.println(KThread.currentThread().getName()
							+ " working "
							+ i
							+ " P: "
							+ ThreadedKernel.scheduler.getPriority(KThread
									.currentThread())
							+ " Ef: "
							+ ThreadedKernel.scheduler
									.getEffectivePriority((KThread
											.currentThread())));
					Machine.interrupt().restore(ee);
					KThread.yield();
				}
				System.out.println(KThread.currentThread().getName()
						+ " finished working");
			}
		});
		selfTestRun(t1, 7, t2, 4);

		/*
		 * Case 3: Tests priority donation
		 * 
		 * This runs t1 with priority 7, t2 with priority 6 and t3 with priority
		 * 4. t1 will wait on a lock, and while t2 would normally then steal all
		 * available CPU, priority donation will ensure that t3 is given control
		 * in order to help unlock t1.
		 */
		System.out.println("\n\n\nCase 3:");
		lock = new Lock();
		condition = new Condition2(lock);
		t1 = new KThread(new Runnable() {
			public void run() {
				lock.acquire();
				System.out.println(KThread.currentThread().getName()
						+ " active");
				lock.release();
			}
		});
		t2 = new KThread(new Runnable() {
			public void run() {
				System.out.println(KThread.currentThread().getName()
						+ " started working");
				for (int i = 0; i < 3; ++i) {
					boolean ee = Machine.interrupt().disable();

					System.out.println(KThread.currentThread().getName()
							+ " working "
							+ i
							+ " P: "
							+ ThreadedKernel.scheduler.getPriority(KThread
									.currentThread())
							+ " Ef: "
							+ ThreadedKernel.scheduler
									.getEffectivePriority((KThread
											.currentThread())));
					Machine.interrupt().restore(ee);

					KThread.yield();
				}
				System.out.println(KThread.currentThread().getName()
						+ " finished working");
			}
		});
		t3 = new KThread(new Runnable() {
			public void run() {
				lock.acquire();
				boolean int_state = Machine.interrupt().disable();
				ThreadedKernel.scheduler.setPriority(2);
				Machine.interrupt().restore(int_state);
				KThread.yield();
				// will now have to realise that t3 owns the lock it wants to
				// obtain
				// t1.acquire()
				// so program execution will continue here.
				System.out.println(KThread.currentThread().getName()
						+ " active ('a' wants its lock back so we are here)");
				lock.release();
				KThread.yield();
				lock.acquire();
				System.out.println(KThread.currentThread().getName()
						+ " active-again (should be after 'a' and 'b' done)");
				lock.release();
			}
		});
		selfTestRun(t1, 6, t2, 4, t3, 7);
	}

	/**
	 * END selfTest
	 ********************************************************************* 
	 */
    /**
     * Allocate a new priority thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer priority from waiting threads
     *					to the owning thread.
     * @return	a new priority thread queue.
     *asigna una nueva thread queue de prioridad
     *transferPriority es verdadero si esta queue debe transferir prioridad
     *para los threads en espera para el trhead propietario.                          
     *retorna una nueva queue de prioridad
     */
    public ThreadQueue newThreadQueue(boolean transferPriority)//inicializa una nueva ThreadQueue y transfiere prioridad
    {
		return new PriorityQueue(transferPriority);//retorna una nueva priorityQueue
    }

    public int getPriority(KThread thread) 
    {
	      	  //hacer enfasis en la diferencia que hay entre .disabled() y .disable()
		Lib.assertTrue(Machine.interrupt().disabled());
		       
		return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) 
    {
		Lib.assertTrue(Machine.interrupt().disabled());       
		return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) 
    {
		Lib.assertTrue(Machine.interrupt().disabled());
		       
		Lib.assertTrue(priority >= priorityMinimum &&
		   priority <= priorityMaximum);
	
		getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority()
    {
		boolean intStatus = Machine.interrupt().disable();
		       
		KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMaximum)
	    return false;

	setPriority(thread, priority+1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    public boolean decreasePriority() 
    {
    boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMinimum)
	    return false;

	setPriority(thread, priority-1);///decrementa prioridad del thread en cuestion

	Machine.interrupt().restore(intStatus);
	return true;
    }
    public static void selfTest() 
    {
	System.out.println("Priority Scheduler Self Test:\n");
	Test1.run();
	System.out.println("");
	Test2.run();
	System.out.println("");
	Test3.run();
	System.out.println("");
	Test4.run();
	System.out.println("");
    }

    /**
     * The default priority for a new thread. Do not change this value.
     * la prioridad que tiene por default un nuevo thread.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;    

    /**
     * Return the scheduling state of the specified thread.
     * retornara el scheduling state the un thread especifico.
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) 
    {
		if (thread.schedulingState == null)
	    	thread.schedulingState = new ThreadState(thread);

		return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     *	un ThreadQueue que ordena los threads por prioridad.
     */
    protected class PriorityQueue extends ThreadQueue 
    {
		PriorityQueue(boolean transferPriority) 
		{
	    	this.transferPriority = transferPriority;
		}//constructor

	public void waitForAccess(KThread thread) 
	{
	    Lib.assertTrue(Machine.interrupt().disabled());
	    getThreadState(thread).waitForAccess(this);
	}//waitForAccess

	public void acquire(KThread thread) 
	{
	    Lib.assertTrue(Machine.interrupt().disabled());
	    ThreadState state=getThreadState(thread);//estado del thread

	    //si yo tengo un poseedor y debo transferir prioridad.
	    //me remuevo de las lista de recursos del poseedor
	    if(this.holder!=null&& this.transferPriority)
	    {
			this.holder.myResource.remove(this);	    	
	    }
	    this.holder=state;
	    state.acquire(this);
	    //getThreadState(thread).acquire(this);
	}

	public KThread nextThread() 
	{
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me
	    if(waitQueue.isEmpty())
	    {
	    	return null;
	    }
	    //si posee holder y se transfiere prioridad
	    //removerme de la lista de recursos del holder.
	    if(this.holder!=null&& this.transferPriority)
	    {
	    	this.holder.myResource.remove(this);
	    }
	    KThread firstThread=pickNextThread();
	    if(firstThread!=null)
	    {
	    	waitQueue.remove(firstThread);
	    	getThreadState(firstThread).acquire(this);
	    }

	    return firstThread;
	}//nextThread()

	/**
	 * Return the next thread that <tt>nextThread()</tt> would return,
	 * without modifying the state of this queue.
	 *
	 * @return	the next thread that <tt>nextThread()</tt> would
	 *		return.
	 */
	protected KThread pickNextThread() 
	{
	    // implement me
	    KThread nextThread=null;
	    for(Iterator<KThread>ts=waitQueue.iterator();ts.hasNext();)
	    {
	    	KThread thread=ts.next();
	    	int priority =getThreadState(thread).getEffectivePriority();
	    	
	    	if(nextThread==null|| priority>getThreadState(nextThread).getEffectivePriority())
	    	{
	    		nextThread=thread;
	    	}//if
	    }//for
	    return nextThread;
	}//pickNextThread
        public int getEffectivePriority() 
        {

            // System.out.print("[Inside getEffectivePriority] transferPriority: " + transferPriority + "\n"); // debug

            // if do not transfer priority, return minimum priority
            if (transferPriority == false) {
            // System.out.print("Inside 'getEffectivePriority:' false branch\n" ); // debug
                return priorityMinimum;
            }

            if (dirty) {
                effectivePriority = priorityMinimum; 
                for (Iterator<KThread> it = waitQueue.iterator(); it.hasNext();) {  
                    KThread thread = it.next(); 
                    int priority = getThreadState(thread).getEffectivePriority();
                    if ( priority > effectivePriority) { 
                        effectivePriority = priority;
                    }
                }
                dirty = false;
            }

            return effectivePriority;
        }//getEffectivePriority()	
	public void setDirty()
	{
		if(transferPriority==false)
		{
			return;
		}//if
		dirty=true;

		if(holder!=null)
		{
			holder.setDirty();
		}//if
	}//setDirty
	public void print() 
	{
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me (if you want)
		for (Iterator<KThread>it=waitQueue.iterator();it.hasNext();) 
		{
			KThread currentThread=it.next();
			int priority=getThreadState(currentThread).getPriority();

			System.out.print("Thread: " +currentThread+"\t Priority: "+priority+"\n");	
		}//for
	}//print

	/**
	 * <tt>true</tt> if this queue should transfer priority from waiting
	 * threads to the owning thread.
	 */
	public boolean transferPriority;

	private LinkedList<KThread> waitQueue=new LinkedList<KThread>();
    
	private ThreadState holder=null;

	private boolean dirty;

	private int effectivePriority;
    }

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */
    protected class ThreadState 
    {
	/**
	 * Allocate a new <tt>ThreadState</tt> object and associate it with the
	 * specified thread.
	 *
	 * @param	thread	the thread this state belongs to.
	 */
	public ThreadState(KThread thread)
	{
	    this.thread = thread;
	    
	    setPriority(priorityDefault);
	}

	/**
	 * Return the priority of the associated thread.
	 *
	 * @return	the priority of the associated thread.
	 */
	public int getPriority() 
	{
	    return priority;
	}

	/**
	 * Return the effective priority of the associated thread.
	 *
	 * @return	the effective priority of the associated thread.
	 */
	public int getEffectivePriority() 
	{
	    // implement me
		int maxEffective=this.priority;

		if(dirty)
		{
			for(Iterator<ThreadQueue>it=myResource.iterator();it.hasNext();)
			{
				PriorityQueue pg=(PriorityQueue)(it.next());
				int effective=pg.getEffectivePriority();
				if(maxEffective<effective)
				{
					maxEffective=effective;
				}//if
			}//for
		}//if(dirty)
	//    return priority;
	return maxEffective;
	}//getEffectivePriority

	/**
	 * Set the priority of the associated thread to the specified value.
	 *
	 * @param	priority	the new priority.
	 */
	public void setPriority(int priority) 
	{
	    if (this.priority == priority)
		return;
	    
	    this.priority = priority;
	    
	    // implement me
	setDirty();
	}//setPriority

	/**
	 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
	 * the associated thread) is invoked on the specified priority queue.
	 * The associated thread is therefore waiting for access to the
	 * resource guarded by <tt>waitQueue</tt>. This method is only called
	 * if the associated thread cannot immediately obtain access.
	 *
	 * @param	waitQueue	the queue that the associated thread is
	 *				now waiting on.
	 *
	 * @see	nachos.threads.ThreadQueue#waitForAccess
	 */
	public void waitForAccess(PriorityQueue waitQueue) 
	{
	    // implement me
		Lib.assertTrue(Machine.interrupt().disabled());
		Lib.assertTrue(waitQueue.waitQueue.indexOf(thread)==-1);

		waitQueue.waitQueue.add(thread);
		waitQueue.setDirty();
		waitingOn=waitQueue;

		if(myResource.indexOf(waitQueue)!=-1)
		{	
			myResource.remove(waitQueue);
			waitQueue.holder=null;
		}//if
	}//waitForAccess

	/**
	 * Called when the associated thread has acquired access to whatever is
	 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
	 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
	 * <tt>thread</tt> is the associated thread), or as a result of
	 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
	 *
	 * @see	nachos.threads.ThreadQueue#acquire
	 * @see	nachos.threads.ThreadQueue#nextThread
	 */
	public void acquire(PriorityQueue waitQueue)
	{
	    // implement me
		Lib.assertTrue(Machine.interrupt().disabled());
		//add waitQueue to myResource list
		myResource.add(waitQueue);	

		if(waitQueue==waitingOn)
		{
			waitingOn=null;
		}//if
		setDirty();
	}//acquire	
	public void setDirty()
	{
		if(dirty)
		{	
			return;	
		}//if
		dirty=true;
		PriorityQueue pg=(PriorityQueue)waitingOn;
		if(pg!=null)
		{
			pg.setDirty();
		}//if	
	}//setDirty
	/** The thread with which this object is associated. */	   
	protected KThread thread;
	/** The priority of the associated thread. */
	protected int priority;
	protected int effectivePriority;

	protected LinkedList<ThreadQueue> myResource=new LinkedList<ThreadQueue>();

	protected ThreadQueue waitingOn;
	//cambia a true cuando la prioridad del Thread es cambiada
	private boolean dirty=false;
    }


}//PriorityScheduler

/**
 * Tests the scheduler with out any priority donation, or different
 * priorities. Yes I know this is kind of pointless considering it's
 * tested by pretty much all the other tests simply running, but meh,
 * its easy.
 * Note, if its run at just the wrong time, then one of the threads
 * can get preempted. This makes the test appear to fail. There is no
 * way to get around this as fork automatically reenables interrupts.
 */
class Test1 
{
    public static void run() 
    {
    	System.out.println("Testeando nuestro  scheduling de forma basica:");
    	KThread threads[] = new KThread[4];//array de KThreads

    	for (int i = 0; i < threads.length; i++) 
    	{
    		threads[i] = new KThread(new Thread(i));
    		threads[i].fork();//aced for a all threads
    	}//for hace fork a todos los threads

	for (int i = 0; i < threads.length; i++)
	{
		threads[i].join();
	}//for hace join a todos los threads
	    
    }//void run

    private static class Thread implements Runnable 
    {
    	private int num;//variable num parameter n
    	public Thread(int n)
    	{
    		num = n;
    	}//recibe un n del for 
    	public void run() 
    	{
    		for (int i = 1; i < 3; i++)
    		{
    			System.out.println("Thread: " + num + " looping");
    			KThread.yield();
    		}//for
    	}//run
    }//class Thread implements Runnable
}//Test1

/**
 * Tests basic scheduling with priorities involved.
 */
class Test2 
{
    public static void run() {
	PriorityScheduler sched = (PriorityScheduler) ThreadedKernel.scheduler;//instancia un objeto PriorityScheduler 

	System.out.println("TESTEANDO MI FUCKING priority scheduling********:");
	KThread threads[] = new KThread[4];

	for (int i = 0; i < threads.length; i++) 
	{
	    threads[i] = new KThread(new Thread(3 - i));
	    boolean intStatus = Machine.interrupt().disable();
	    sched.setPriority(threads[i], 7 - i);//setea prioridad de threads en ciclo y les resta i
	    Machine.interrupt().restore(intStatus);
	    threads[i].fork();
	}//for

	for (int i = 0; i < threads.length; i++)
	{
		threads[i].join();
	}//hace join de todos los threads
	    
    }

    private static class Thread implements Runnable {
	private int num;
	public Thread(int n) {
	    num = n;
	}
	public void run() {
	    for (int i = 1; i < 3; i++) {
		System.out.println("Priority: " + num + " looping");
		KThread.yield();
	    }
	}
    }
}//Test2

/**
 * Tests priority donation by running 4 threads, 2 with equal priority
 * and the other with higher and lower priority. The high priority thread
 * then waits on the low priority one and we see how long it takes to get
 * scheduled.
 */
class Test3 
{
    static boolean high_run = false;

    public static void run() {
	Lock l = new Lock();
	PriorityScheduler sched = (PriorityScheduler) ThreadedKernel.scheduler;

	System.out.println("Testing basic priority inversion:");

	KThread low = new KThread(new Low(l));
	KThread med1 = new KThread(new Med(1));
	KThread med2 = new KThread(new Med(2));
	KThread high = new KThread(new High(l));

	boolean intStatus = Machine.interrupt().disable();
	sched.setPriority(high, 4);
	sched.setPriority(med1, 3);
	sched.setPriority(med2, 3);
	sched.setPriority(low, 1);
	Machine.interrupt().restore(intStatus);

	low.fork();
	KThread.yield();
	med1.fork();
	high.fork();
	med2.fork();
	KThread.yield();

	/* Make sure its all finished before quitting */
	low.join();
	med2.join();
	med1.join();
	high.join();
    }

    private static class High implements Runnable {
	private Lock lock;

	public High(Lock l) {
	    lock = l;
	}

	public void run() {
	    System.out.println("High priority thread sleeping");
	    lock.acquire();
	    Test3.high_run = true;
	    System.out.println("High priority thread woken");
	    lock.release();
	}
    }

    private static class Med implements Runnable {
	int num;
	public Med(int n) {
	    num = n;
	}
	public void run() {
	    for (int i = 1; i < 3; i++)
		KThread.yield();

	    if (Test3.high_run)
		System.out.println("High thread finished before thread " + num + ".");
	    else
		System.out.println("Error, meduim priority thread finished"
				   + " before high priority one!");
	}
    }

    private static class Low implements Runnable {
	private Lock lock;

	public Low(Lock l) {
	    lock = l;
	}

	public void run() {
	    System.out.println("Low priority thread running");
	    lock.acquire();
	    KThread.yield();
	    System.out.println("Low priority thread finishing");
	    lock.release();
	}
    }
}//Test3

/**
 * A more advanced priority inversion test.
 */
class Test4 
{
    static boolean high_run = false;

    public static void run() {
	Lock l1 = new Lock();
	Lock l2 = new Lock();
	Lock l3 = new Lock();
	PriorityScheduler sched = (PriorityScheduler) ThreadedKernel.scheduler;

	System.out.println("Testing complex priority inversion:");

	KThread t1 = new KThread(new Thread(l1, 1));
	KThread t2 = new KThread(new Thread(l2, l1, 2));
	KThread t3 = new KThread(new Thread(l3, l2, 3));
	KThread t4 = new KThread(new Thread(l3, 4));

	t1.fork();
	t2.fork();
	t3.fork();
	t4.fork();

	KThread.yield();

	boolean intStatus = Machine.interrupt().disable();
	sched.setPriority(t4, 3);
	if (sched.getEffectivePriority(t1) != 3)
	    System.out.println("Priority not correctly donated.");
	else
	    System.out.println("Priority correctly donated.");
	Machine.interrupt().restore(intStatus);

	KThread.yield();

	intStatus = Machine.interrupt().disable();
	if (sched.getEffectivePriority(t1) != 1)
	    System.out.println("Priority donation not revoked.");
	else
	    System.out.println("Priority donation correctly revoked.");
	Machine.interrupt().restore(intStatus);


	/* Make sure its all finished before quitting */
	t1.join();
	t2.join();
	t3.join();
	t4.join();
    }

    private static class Thread implements Runnable {
	private Lock lock;
	private Lock altLock;
	private int num;

	public Thread(Lock l, int n) {
	    lock = l;
	    num = n;
	    altLock = null;
	}

	public Thread(Lock l, Lock a, int n) {
	    lock = l;
	    num = n;
	    altLock = a;
	}

	public void run() {
	    System.out.println("Thread: " + num + " sleeping");
	    lock.acquire();
	    if (altLock != null)
		altLock.acquire();

	    KThread.yield();

	    System.out.println("Thread: " + num + " woken");
	    if (altLock != null)
		altLock.release();
	    lock.release();
	}
    }
}//Test4