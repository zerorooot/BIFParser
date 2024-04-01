# BIFParser
Parse emby bif files
# how to use
```
git clone https://github.com/zerorooot/BIFParser.git
cd BIFParser
java BIFParser.java "you_bif_file" "image width" "image high"
```
**For example**
```
java BIFParser.java demo-320-10.bif 320 180
```
The output results are saved in the "demo-320-10" folder

# BIF file specification

The following specification describes the implementation of the BIF (Base Index Frames) file archive. The BIF archive is used to encapsulate a set of still images for supporting video trick modes (e.g. FF/REW) on the Roku Streaming Player. This format has been optimized for the usage pattern inherent in this model and is well-suited and capable of facilitating those patterns in an efficient manner.

## Requirements and usage patterns

It is important that the file format have the following features:

*   It must be easy to find and interpret the archive metadata.
*   It must be network-access friendly.
*   It must minimize levels of indirection.
*   It must be compact.
*   It must easily accommodate the entire range of possible data.

The format should also be capable of providing future extensions should they be needed.

## Conventions

This specification assumes that all values are stored little-endian.

## File format

All multibyte integers are stored in little-endian format. That is, the first byte is the least significant byte and the last byte is the most significant.

## Magic number

This is a file identifier. It contains enough information to identify the file type uniquely.

<div class="hscroll">

<table>

<thead>

<tr>

<th class="short-line">byte</th>

<th class="short-line">0</th>

<th class="short-line">1</th>

<th class="short-line">2</th>

<th class="short-line">3</th>

<th class="short-line">4</th>

<th class="short-line">5</th>

<th class="short-line">6</th>

<th class="short-line">7</th>

</tr>

</thead>

<tbody>

<tr>

<td class="short-line">value</td>

<td class="short-line">`0x89`</td>

<td class="short-line">`0x42`</td>

<td class="short-line">`0x49`</td>

<td class="short-line">`0x46`</td>

<td class="short-line">`0x0d`</td>

<td class="short-line">`0x0a`</td>

<td class="short-line">`0x1a`</td>

<td class="short-line">`0x0a`</td>

</tr>

</tbody>

</table>

</div>

## Version

This space is reserved for a revision number. The current specification is file format version 0\. The value should be incremented for non-backward-compatible revisions of this document.

<div class="hscroll">

<table>

<thead>

<tr>

<th class="short-line">byte</th>

<th class="short-line">8 9 10 11</th>

</tr>

</thead>

<tbody>

<tr>

<td class="short-line">value</td>

<td class="short-line">Version</td>

</tr>

</tbody>

</table>

</div>

## Number of BIF images

This is an unsigned 32-bit value (N) that represents the number of BIF images in the file. The number of entries in the index will be N+1, including the end-of-data entry.

<div class="hscroll">

<table>

<thead>

<tr>

<th class="short-line">byte</th>

<th class="short-line">12 13 14 15</th>

</tr>

</thead>

<tbody>

<tr>

<td class="short-line">value</td>

<td class="short-line">Number of BIF images (N)</td>

</tr>

</tbody>

</table>

</div>

## Framewise separation

This specifies the denomination of the frame timestamp values. In order to obtain the "real" timestamp (in milliseconds) of a frame, this value is multiplied by the timestamp entry in the BIF index. If this value is 0, the timestamp multiplier shall be 1000 milliseconds.

<div class="hscroll">

<table>

<thead>

<tr>

<th class="short-line">byte</th>

<th class="short-line">16 17 18 19</th>

</tr>

</thead>

<tbody>

<tr>

<td class="short-line">value</td>

<td class="short-line">Timestamp Multiplier (in milliseconds)</td>

</tr>

</tbody>

</table>

</div>

## Reserved

These bytes are reserved for future expansion. They shall be 0.

<div class="hscroll">

<table>

<thead>

<tr>

<th class="short-line">byte</th>

<th class="short-line">20 ... 63</th>

</tr>

</thead>

<tbody>

<tr>

<td class="short-line">value</td>

<td class="short-line">0x00 / / 0x00</td>

</tr>

</tbody>

</table>

</div>

## BIF index

This space is used for the BIF index entries. There are N+1 entries. Each entry contains two unsigned 32-bit values.

<div class="hscroll">

<table>

<thead>

<tr>

<th class="short-line">byte</th>

<th class="short-line">64 65 66 67</th>

<th class="short-line">68 69 70 71</th>

</tr>

</thead>

<tbody>

<tr>

<td class="short-line">index 0</td>

<td class="short-line">Frame 0 timestamp</td>

<td class="short-line">absolute offset of frame</td>

</tr>

<tr>

<td class="short-line">index 1</td>

<td class="short-line">Frame 1 timestamp</td>

<td class="short-line">absolute offset of frame</td>

</tr>

<tr>

<td class="short-line">index 2</td>

<td class="short-line">Frame 2 timestamp</td>

<td class="short-line">absolute offset of frame</td>

</tr>

<tr>

<td class="short-line">...</td>

<td class="short-line"></td>

<td class="short-line"></td>

</tr>

<tr>

<td class="short-line">index N-1</td>

<td class="short-line">Frame N-1 timestamp</td>

<td class="short-line">absolute offset of frame</td>

</tr>

<tr>

<td class="short-line">index N</td>

<td class="short-line">0xffffffff</td>

<td class="short-line">last byte of data + 1</td>

</tr>

</tbody>

</table>

</div>

Because the size of each BIF is determined by subtracting its offset from the offset of the next entry in the index, the BIFs shall appear in the index in the same order as they appear in the data section, and they shall be adjacent.

The absolute timstamps of the BIF captures can be obtained by multiplying the frame timestamp by the timestamp multiplier.
