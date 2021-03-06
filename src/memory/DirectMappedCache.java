package memory;

import java.util.ArrayList;

import util.Utils;

//
// CACHE ENTRY
//
class Entry {
  int value;        // the line's word (1 cache-line = 1 word)
  int tag;          // the line's tag
  boolean isValid;  // the validity bit
  public boolean dirtyBit;
}

public class DirectMappedCache implements Memory {

  //
  // ATTRIBUTES
  //
  private final ArrayList<Entry> entries;
  private final int accessTime;
  private final Memory memory;

  private final int indexWidth;
  private final int indexMask;

  private int operationTime;
  private final Stats stats;

  //
  // CONSTRUCTORS
  //
  public DirectMappedCache(int size, int accessTime, Memory memory) {
    if (size <= 0) {
      throw new IllegalArgumentException("size");
    }
    if (accessTime <= 0) {
      throw new IllegalArgumentException("accessTime");
    }
    if (memory == null) {
      throw new NullPointerException("memory");
    }
    indexWidth = Utils.log(size);
    if (indexWidth == -1) {
      throw new IllegalArgumentException("size");
    }
    this.indexMask = Utils.mask(indexWidth);

    this.entries = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      entries.add(new Entry());
    }
    this.accessTime = accessTime;
    this.memory = memory;

    this.stats = new Stats();
  }

  //
  // Memory INTERFACE
  //
  @Override
  public int read(int address) {
    Entry entry = entries.get(toIndex(address));
    if (entry.isValid && entry.tag == toTag(address)) {
      // hit
      operationTime = accessTime;
      stats.reads.add(true, operationTime);
      entry.dirtyBit = false;
    } else {
      // miss
      entry.value = memory.read(address);
      entry.tag = toTag(address);
      entry.isValid = true;
      operationTime = memory.getOperationTime() + accessTime;
      stats.reads.add(false, operationTime);
      entry.dirtyBit = true;
    }
    return entry.value;
  }

  @Override
  public void write(int address, int value) {
    writeAround(address, value);
  }

  private void writeAround(int address, int value) {
      
      
    Entry entry = entries.get(toIndex(address));
    if (entry.dirtyBit ==false)
    {
        entry.value = value;
        entry.dirtyBit = true;
    }
    else
    {
       memory.write(address, entry.value);
       entry.value = value;
    }
      
    // Implémentation du write through
      /*
     memory.write(address, value);
     this.entries.get(toIndex(address)).value = value;
    operationTime = memory.getOperationTime() + accessTime;
    stats.writes.add(false, operationTime);*/
      
      
      
      
      
      //Write Around
      /*Entry entry = entries.get(toIndex(address));
    if (entry.isValid && entry.tag == toTag(address)) {
      entry.isValid = false;
    }
    memory.write(address, value);
    operationTime = memory.getOperationTime() + accessTime;
    stats.writes.add(false, operationTime);*/
  }

  private void writeThrough(int address, int value) {
      
    Entry entry = entries.get(toIndex(address));    
    memory.write(address, value);
    entry.value = value;
    operationTime = memory.getOperationTime() + accessTime;
    stats.writes.add(false, operationTime);
  }

  private void writeBack(int address, int value) {
    Entry entry = entries.get(toIndex(address));
    if (entry.dirtyBit == false) 
    {
        entry.value = value;
        entry.dirtyBit = true;
    }
    else
    {
       memory.write(address, entry.value);
       entry.value = value;
       entry.dirtyBit =true;
    }
// TODO
  }
public void flush()
{
    for (int i = 0 ; i < entries.size(); i ++)
    {
        if (entries.get(i).dirtyBit == true)
        {
            memory.write(entries.get(i).tag, entries.get(i).value);
        }
    }
}
  @Override
  public int getOperationTime() {
    return operationTime;
  }

  @Override
  public Stats getStats() {
    return stats;
  }

  //
  // UTILITIES
  //
  private int toIndex(int address) {
    return address & indexMask;
  }

  private int toTag(int address) {
    return address >> indexWidth;
  }

  private int toAddress(int tag, int index) {
    return (tag << indexWidth) + index;
  }

}
