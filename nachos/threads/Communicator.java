package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator 
{
    /**
     * Allocate a new communicator.
     */
	private Lock dataLock;//este el lock
    private Condition2 speakerWait;//speaker
    private Condition2 listenerWait;//listener
	
    private boolean wordBufferFull;//Buffer de memoria
    private int sharedMemory;//memoria compartida

    private LinkedList<Integer> msgQueue;//cola de msg
	private LinkedList<KThread> listenerQueue;//

	private int waitingListeners;
	
    public Communicator() 
    {
    	dataLock = new Lock();
        speakerWait = new Condition2(dataLock);//speaker apunta a lock
        listenerWait = new Condition2(dataLock);//listener apunta a lock
//se inicializan variables de estado
    	msgQueue = new LinkedList<Integer>();
    	listenerQueue = new LinkedList<KThread>();

    	waitingListeners = 0;
        //********************
        wordBufferFull=false;//hay o no hay palabra;
    }//Communicator constructor

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    
    //A potential problem with the current implementation
    //Thread 1 listens
    //thread 2 listens
    //thread 3 speaks
    //thread 4 speaks
    //--I don't think it is guaranteed that 1 listens to 3 and
    //  2 listens to 4 (not sure if this is a requirement though)
    public void speak(int word) 
    {
    /*   	dataLock.acquire();
    	while(waitingListeners == 0)
    	{
    		listenerWait.wakeAll();
    		speakerWait.sleep();
    	}
    	waitingListeners--;
    	msgQueue.addLast(word);
    	listenerWait.wakeAll();
    	dataLock.release();
    	return;*/
    	
    	dataLock.acquire();//obtiene el lock
    	//we have to wait for someone to listen to what has been spoken before we speak again
        while(wordBufferFull)
        {
            listenerWait.wakeAll();
            speakerWait.sleep();//duerme mientras no hay listeners, espera hasta que lo escuchen
        }//mientras no hayan listeners se queda dormido
    	
        sharedMemory=word;	
    
       // msgQueue.addLast(word);//palabra que dio speaker
    	
        wordBufferFull=true;
        
        listenerWait.wakeAll();//hace un wakeAll para que todos los listener escuchen, despierten        
        
        waitingListeners--;//por ende si hay una palabra entonces hay un listener menos
        
        speakerWait.sleep();//espera hasta que alguien lo escuche

    	dataLock.release();//libera el lock*/
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() 
    {	
    	/*int word = 0;
    	dataLock.acquire();
    	waitingListeners++;
    	listenerQueue.addLast(KThread.currentThread());
    	speakerWait.wake();
    	do{
    		listenerWait.sleep();
    	}while(msgQueue.size() == 0 || KThread.currentThread() != listenerQueue.peekFirst());
    	listenerQueue.removeFirst();
    	word = msgQueue.remove();
    	if(listenerQueue.size() > 0 && msgQueue.size() > 0)
    		listenerWait.wakeAll();
    	dataLock.release();
    	return word;*/
    	
        dataLock.acquire();//obtienen el lock    	
        
        //Waiting for the speaker to speak}
        while(!wordBufferFull)
        {
            speakerWait.wakeAll();
            listenerWait.sleep();
        }//while
        int word = sharedMemory;
        wordBufferFull=false;
    	//waitingListeners++;//hay un listener 
    	//listenerQueue.addLast(KThread.currentThread());//en la cola de listener se hadiere el currentThread
    	speakerWait.wakeAll();//notifica al speakerWait para que despierte ya que esta listo para escuchar
    	dataLock.release();
    	return word;
    	/*do{
    		listenerWait.sleep();
    	}while(msgQueue.size() == 0 || KThread.currentThread() != listenerQueue.peekFirst());
    	listenerQueue.removeFirst();
    	word = msgQueue.remove();
    	if(listenerQueue.size() > 0 && msgQueue.size() > 0)
    		listenerWait.wakeAll();
    	dataLock.release();
    	return word;*/
    }//listen
//*** EVERYTHING BELOW HERE IS JUST FOR TESTING ***
    private static class CommunicatorSendTest implements Runnable 
    {
        private String name;
        private Communicator communicator; 
        private int word;
        CommunicatorSendTest(String name, Communicator communicator, int word) 
        {
            this.name=name;
            this.communicator=communicator;
            this.word=word;
        }
    
    public void run() 
    {
        System.out.println("*** " + name + " ===> antes de llamada a speak con " + word);
        communicator.speak(word);
        System.out.println("*** " + name + " ===> despues de llamada a speak con " + word+" *****");
    }
}//CommunicatorSendTest
    private static class CommunicatorListenTest implements Runnable 
    {
        private String name;
        private Communicator communicator; 
        CommunicatorListenTest(String name, Communicator communicator) 
        {
            this.name=name;
            this.communicator=communicator;
        }
    
        public void run() 
        {
            System.out.println("*** " + name + " ===> antes de llamar a listen.");
            int word=communicator.listen();
            System.out.println("*** " + name + " ===> despues de llamar a listen. Recibimos " + word+" ######");
        }//run
    }//CommunicatorListenTest
    public static void selfTest() 
    {
    // Communicator Tests
        Communicator communicator = new Communicator();
        new KThread(new CommunicatorListenTest("listener 1",communicator)).fork();
        new KThread(new CommunicatorListenTest("listener2",communicator)).fork();  
        new KThread(new CommunicatorListenTest("listener3",communicator)).fork();
        new KThread(new CommunicatorListenTest("listener4",communicator)).fork(); 
        new KThread(new CommunicatorSendTest("speaker1",communicator,10)).fork();
        new KThread(new CommunicatorSendTest("speaker2",communicator,20)).fork();  
              

 
    }//selfTest()    
}
