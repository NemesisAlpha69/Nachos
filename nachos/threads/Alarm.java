package nachos.threads;

import nachos.machine.*;
import java.util.TreeSet;
import java.util.*;
//import java.util.Iterator;
//import java.util.SortedSet;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm 
{
	LinkedList<WaitingKThread> timerListKThreads=new LinkedList<WaitingKThread>();
	/**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() 
    {
    //waiting =new TreeSet<WaitingThread>();
    	Machine.timer().setInterruptHandler(new Runnable()
    	{
    		public void run() { timerInterrupt(); }
	    });
    }//Alarm Contructor

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() 
    {
    	for(int i=timerListKThreads.size()-1;i>=0;i--)
    	{
    		if(Machine.timer().getTime()>=timerListKThreads.get(i).getExpireTime())
    		{
    			System.out.println(Machine.timer().getTime()+"Machine.timer().getTime() >= " +timerListKThreads.get(i).getExpireTime()+"timerListKThreads******");
    			timerListKThreads.get(i).getThreadName().ready();
    			timerListKThreads.remove(i);
    		}//if
    	}//for
    	
    	KThread.yield();
    }//timerInterrupt()

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) 
    {
 	   boolean intStatus = Machine.interrupt().disable();
       WaitingKThread current = new WaitingKThread(KThread.currentThread(),Machine.timer().getTime()+x);
       
       //current.wakeUpTime = Machine.timer().getTime() + x;//este sera el tiempo que esperara el thread para que despierte
       timerListKThreads.add(current);
       System.out.println(current.getThreadName()+" currentThreadName");
       // sleeping the current thread, on wake up: restore interrupts
       KThread.sleep();
       Machine.interrupt().restore(intStatus);
    }
    private static final char dbgInt='i';
    //private TreeSet<WaitingThread> waiting;
    
  private class WaitingKThread
    {
    	WaitingKThread(KThread thread,long time)
    	{
    		this.time=time;
    		this.thread=thread;
    	}//constructor
    	
    	public KThread getThreadName()
    	{
    		return this.thread;
    	}//method getThreadName
    	public long getExpireTime()
    	{
    		return time;
    	}//getExpireTime
    	long time;
    	KThread thread;
    }//private class WaitingThread*/
    private static class AlarmTest implements Runnable
    {
    	AlarmTest(long x)
    	{
    		this.time=x;
    	}//constructor
    	public void run()
    	{
    		System.out.println(KThread.currentThread().getName()+" **** alarm ****");
    		ThreadedKernel.alarm.waitUntil(time);
    		System.out.println(KThread.currentThread().getName()+ " **** woken  up ****");
    	}//run()
    private long time;
    }//AlarmTest class
    
    public static void selfTest()
    {
    	Lib.debug(dbgThread,"********Entrando a Alarm.selfTest 1 ********");
    	System.out.println("Entrando alarm.seftTest 1");
    	Runnable r= new Runnable()
    	{
    		public void run()
    		{
    			KThread t[]=new KThread[10];
    			
    			for(int i=0;i<10;i++)
    			{
    				t[i]=new KThread(new AlarmTest(1600000000));
    				t[i].setName("Thread"+i).fork();
    			}//for()
    			for(int i=0;i<100000;i++)
    			{
    				KThread.yield();
    			}//for2
    		}//run()
    	};//Runnable
    	KThread t=new KThread(r);
    	t.setName("Alarm SelfTest");
    	t.fork();
    	KThread.yield();
    	t.join();
    	System.out.println("salieno de  Alarm.selfTest");
    }//selfTest()
   public static void selfTest2()
   {
	   Lib.debug(dbgInt, "###################Entrando a Alarm.selfTest2()##########");
	   System.out.println("########Entrando a Alarm.selfTest2()*******###################");
	   Alarm a1=new Alarm();
	   System.out.println("in waitUntil a1");	   
	   a1.waitUntil(10000000);
	   System.out.println("out waitUntil a1");
	  // System.out.println("")
	   System.out.println("in waitUntil a2");
	   Alarm a2=new Alarm();
	   a2.waitUntil(10000000);
	   System.out.println("out waitUntil a2");
	   
	   
	   System.out.println("in waitUntil a3");
	   Alarm a3=new Alarm();
	   a3.waitUntil(10000000);
	   System.out.println("out waitUntil a3");

//**********************************Defino en set de ejecucion******************
	   Runnable r1=new Runnable()
	   {
		   public void run()
		   {
			   
			   KThread kt=new KThread();
		   }
	   };//Runnable()
   
   }//selfTest2()
    private static final char dbgThread='t';
    
}//class
