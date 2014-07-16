package uvm.refimpl.mem.los;

import uvm.util.ErrorUtils;

public class FreeList {
    private int nUnits;

    private boolean isUsed[];
    private boolean isMulti[];
    private int prev[];
    private int next[];
    private int size[];

    private int head; // first free region.

    public FreeList(int nUnits) {
        this.nUnits = nUnits;
        isUsed = new boolean[nUnits];
        isMulti = new boolean[nUnits];
        prev = new int[nUnits];
        next = new int[nUnits];
        size = new int[nUnits];

        ErrorUtils.uvmAssert(nUnits > 1,
                "Why use such a small \"large\" object space?");

        isUsed[0] = false;
        isMulti[0] = true;
        prev[0] = -1;
        next[0] = -1;
        size[0] = nUnits;
        isUsed[nUnits - 1] = false;
        isMulti[nUnits - 1] = true;
        size[nUnits - 1] = nUnits;

        head = 0;
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
        if (requiredSize == 1) {
            return head;
        }

        for (int cur = head; cur != -1; cur = next[cur]) {
            if (isMulti[cur] && size[cur] <= requiredSize) {
                return cur;
            }
        }

        return -1;
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
        int thisPrev = prev[freeStart];
        int thisNext = next[freeStart];
        int thisSize = size[freeStart];

        isUsed[freeStart] = true;
        if (allocSize == 1) {
            isMulti[freeStart] = false;
        } else {
            isMulti[freeStart] = true;
            size[freeStart] = allocSize;
            int newUsedLast = freeStart + allocSize - 1;
            isUsed[newUsedLast] = true;
            isMulti[newUsedLast] = true;
            size[newUsedLast] = allocSize;
        }

        int newFreeSize = thisSize - allocSize;
        if (newFreeSize > 0) {
            int newFreeStart = freeStart + allocSize;
            isUsed[newFreeStart] = false;
            if (newFreeSize == 1) {
                isMulti[newFreeStart] = false;
            } else {
                isMulti[newFreeStart] = true;
                size[newFreeStart] = newFreeSize;
                int newLast = newFreeStart + newFreeSize - 1;
                isUsed[newLast] = false;
                isMulti[newLast] = true;
                size[newLast] = newFreeSize;
            }

            prev[newFreeStart] = thisPrev;
            next[newFreeStart] = thisNext;

            if (thisPrev != -1) {
                next[thisPrev] = newFreeStart;
            }
            if (thisNext != -1) {
                prev[thisNext] = newFreeStart;
            }

            if (head == freeStart) {
                head = newFreeStart;
            }
        } else {
            if (thisPrev != -1) {
                next[thisPrev] = thisNext;
            }
            if (thisNext != -1) {
                prev[thisNext] = thisPrev;
            }
            if (head == freeStart) {
                head = -1;
            }
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
        int thisSize = getSize(usedStart);

        int thisLeft = usedStart - 1;
        int newStart = thisLeft == -1 || isUsed[thisLeft] ? usedStart
                : usedStart - getSize(thisLeft);

        int thisRight = usedStart + thisSize;
        int newEnd = thisRight == nUnits || isUsed[thisRight] ? thisRight
                : thisRight + size[thisRight];

        isUsed[newStart] = false;
        int newSize = newEnd - newStart;
        if (newSize == 1) {
            isMulti[newStart] = false;
        } else {
            isMulti[newStart] = true;
            size[newStart] = newSize;
            int newLast = newEnd - 1;
            isUsed[newLast] = false;
            isMulti[newLast] = true;
            size[newLast] = newSize;
        }

        if (head == -1 || head == usedStart) {
            head = newStart;
        }
    }

    /**
     * Get the size of a region
     * 
     * @param i
     *            the first or the last block of a region
     * @return the size of the region
     */
    private int getSize(int i) {
        boolean thisMulti = isMulti[i];
        int thisSize = thisMulti ? size[i] : 1;
        return thisSize;
    }
}
