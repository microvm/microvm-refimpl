package uvm.refimpl.mem.los;

import uvm.util.ErrorUtils;

public class FreeList {
    private static final boolean DEBUG = false;

    private int nUnits;

    private boolean isUsed[];
    private boolean isMulti[];
    private int prev[];
    private int next[];
    private int size[];

    private int head; // point to any free region

    public FreeList(int nUnits) {
        this.nUnits = nUnits;
        isUsed = new boolean[nUnits];
        isMulti = new boolean[nUnits];
        prev = new int[nUnits];
        next = new int[nUnits];
        size = new int[nUnits];

        ErrorUtils.uvmAssert(nUnits > 1,
                "Why use such a small \"large\" object space?");

        head = -1;

        setSizeAndIsUsed(0, nUnits, false);
        link(0);
    }

    /**
     * Allocate requiredSize contiguous units of resources.
     * 
     * @param requiredSize
     *            the required size (in units)
     * @return the index of the first unit of the allocated region, or -1 if not
     *         available.
     */
    public int allocate(int requiredSize) {
        int region = firstFit(requiredSize);
        if (region != -1) {
            allocInto(region, requiredSize);
            return region;
        } else {
            return -1;
        }
    }

    /**
     * Deallocate a previously allocated region.
     * 
     * @param region
     *            the index of the first block of the region.
     */
    public void deallocate(int region) {
        deallocAndMerge(region);
    }

    /**
     * Find the first region that has 'size' units available.
     * 
     * @param requiredSize
     *            the required size (in units)
     * @return the index of the first unit of the region, or -1 if not found.
     */
    private int firstFit(int requiredSize) {

        if (head == -1) {
            return -1;
        } else {
            int cur = head;

            do {
                if (getSize(cur) >= requiredSize) {
                    return cur;
                }
                cur = next[cur];
            } while (cur != head);

            return -1;
        }
    }

    /**
     * Allocate some units into the beginning of a free region. Shrink it and
     * relink its neighbours.
     * 
     * @param freeUnit
     *            the first unit of the region to allocate in
     * @param allocSize
     *            size (in units) of allocation
     */
    private void allocInto(int freeStart, int allocSize) {
        int thisStart = freeStart;
        debugLog("Allocate %d units into %d...\n", allocSize,
                thisStart);
        int thisPrev = prev[thisStart];
        int thisNext = next[thisStart];
        int thisSize = getSize(thisStart);

        debugLog("thisPrev = %d\n", thisPrev);
        debugLog("thisNext = %d\n", thisNext);
        debugLog("thisSize = %d\n", thisSize);

        unlink(thisStart);

        setSizeAndIsUsed(thisStart, allocSize, true);

        int newFreeSize = thisSize - allocSize;
        if (newFreeSize > 0) {
            int newFreeStart = thisStart + allocSize;
            setSizeAndIsUsed(newFreeStart, newFreeSize, false);

            link(newFreeStart);
        }
    }

    /**
     * Deallocate a used region and join the newly freed region with its
     * neighbours if there are any.
     * 
     * @param usedStart
     *            the first unit of the used region.
     */
    private void deallocAndMerge(int usedStart) {
        int thisStart = usedStart;
        int thisSize = getSize(usedStart);
        int thisLeft = usedStart - 1;
        int thisRight = usedStart + thisSize;

        int newStart;
        int newEnd;

        if (thisLeft == -1 || isUsed[thisLeft]) {
            newStart = thisStart;
        } else {
            newStart = getStartFromLast(thisLeft);
            unlink(newStart);
        }

        if (thisRight == nUnits || isUsed[thisRight]) {
            newEnd = thisRight;
        } else {
            newEnd = thisRight + getSize(thisRight);
            unlink(thisRight);
        }

        int newSize = newEnd - newStart;
        setSizeAndIsUsed(newStart, newSize, false);
        link(newStart);
    }

    private void link(int st) {
        if (head == -1) {
            head = st;
            prev[st] = next[st] = st;
        } else {
            prev[st] = prev[head];
            next[st] = head;
            next[prev[head]] = st;
            prev[head] = st;
        }
    }

    private void unlink(int st) {
        if (next[st] == st) {
            head = -1;
        } else {
            head = next[st];
            prev[head] = prev[st];
            next[prev[st]] = head;
        }
    }

    private int getStartFromLast(int last) {
        return last - getSize(last) + 1;
    }

    /**
     * Get the size of a region
     * 
     * @param regionStartOrLast
     *            the first or the last block of a region
     * @return the size of the region
     */
    private int getSize(int regionStartOrLast) {
        boolean thisMulti = isMulti[regionStartOrLast];
        int thisSize = thisMulti ? size[regionStartOrLast] : 1;
        return thisSize;
    }

    /**
     * Set the size of a region. Updates both isMulti and size and updates both
     * ends if multi.
     * 
     * @param regionStart
     *            The first block of a region
     * @param sz
     *            the size of the region.
     */
    private void setSizeAndIsUsed(int regionStart, int sz, boolean used) {
        if (sz == 1) {
            isUsed[regionStart] = used;
            isMulti[regionStart] = false;
        } else {
            isUsed[regionStart] = used;
            isMulti[regionStart] = true;
            size[regionStart] = sz;
            int regionLast = regionStart + sz - 1;
            isUsed[regionLast] = used;
            isMulti[regionLast] = true;
            size[regionLast] = sz;
        }
    }

    public void debugPrintList() {
        debugLog("head=%d\n", head);
        int multiSkipTo = 0;
        for (int i = 0; i < nUnits; i++) {
            String multiSkip;
            if (i < multiSkipTo - 1) {
                multiSkip = " SKIPPED";
            } else {
                multiSkip = "";
            }
            debugLog("%d [%s%s] %d (%d %d)%s\n", i, isUsed[i] ? "U"
                    : " ", isMulti[i] ? "m" : " ", size[i], prev[i], next[i],
                    multiSkip);
            if (i >= multiSkipTo && isMulti[i]) {
                multiSkipTo = i + size[i];
            }
        }
    }

    private void debugLog(String fmt, Object... args) {
        if (DEBUG) {
            System.out.printf(fmt, args);
        }
    }
}
