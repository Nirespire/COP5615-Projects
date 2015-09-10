# Project 1

## Contributors
  1. Sanjay Nair
  2. Preethu Thomas

## Structure
<pre>
+______________________________________________________+
|                      _______                         |
|                   / |Worker1|  \                     |
|    ____________     |_______|        _____________   |
|   |WorkAssigner|  _     :      _    |FindIndicator|  |   HOST1
|   |____________|     _______        |_____________|  |
|                   \ |WorkerN|  /                     |
|                     |_______|                        |
+______________________________________________________+
                    \            /              
+______________________________________________________+
|                      _______                         |
|                     |Worker1|                        |
|                     |_______|                        |   HOST2
|                         :                            |
|                      _______                         |
|                     |WorkerN|                        |
|                     |_______|                        |
+______________________________________________________+
</pre>

  Work unit is not the number of strings being sent to each
  worker, but some indication of where that workers assigment of work starts and ends
  the workAssigner class needs to be changed to be a WorkAssigner
  This realization came one you see how the workers and workAssigner interacts, they send each other messages
  If workAssigner is expected to generate the string, there may be times, where other workers are idle
  Each worker should be given enough work without keeping the workAssigner class too busy/ too relaxed is the ideal
  work unit.

Instead send a seed string, and each worker will have to try say adding all combinations
of three chars appended to given seed,
example,if WORK_UNIT is 3 and the seed send to the worker is "a", worker is expected to try everything
from snairaaaa to sanirazzz, in this way we are assigning each worker a unique set of stuff to try
among themselves.
The only exception is when a blank seed ("") is  sent to a worker, that work will try all
0 to 3 length string combinations , e.g. snair, snaira, sanirb,... sanirzzz.
We need to see which work unit i.e. number of combinations(2,3,4, or more) should
each worker try, and which is most efficient.

## Submission README

1. Work unit that resulted in best performance:

    Each work unit in this implementation means how large of a space each worker is assigned to hash through at a time. The WorkAssigner actor is responsible for generating "seed" strings that are sent to each worker along with the work unit value. This combination tells the worker what space to search and when it's work is done. 
    For example, the work assigner could send one worker a seed = "aa" with a work unit of 2. That means the worker is responsible for hashing all strings from "aa", "aaa", "aab", "aac"... to "aazz". This example assumes that the entire ASCII range is from "a" to "z", however each worker actually considers characters from ASCII value 32 (space) to 126 (~).

    CPU: Intel(R) Core(TM) i3-4030U CPU @ 1.90GHz (fam: 06, model: 45, stepping: 01)
    Memory: 3895492K/4096180K available

    The below table shows cpu/real time ratio, a larger number shows better CPU utilization; WU - shows work units, the length of string whose combination will be tried by each worker. WRK1, WRK2, ... , WRK10 indicates 1,2,...,10 worker per host respectively.

<pre>
    +---------------------------------------------------------------------------+
    | WU | WRK1 | WRK2 | WRK3 | WRK4 | WRK5 | WRK6 | WRK7 | WRK8 | WRK9 | WRK10 |
    +---------------------------------------------------------------------------+
    | 1  | 1.19 | 2.11 | 2.78 | 3.67 | 3.77 | 3.80 | 3.76 | 3.80 | 3.83 | 3.83  |
    | 2  | 1.17 | 2.08 | 2.99 | 3.83 | 3.83 | 3.84 | 3.84 | 3.82 | 3.85 | 3.84  |
    | 3  | 1.16 | 2.08 | 2.94 | 3.85 | 3.85 | 3.85 | 3.89 | 3.88 | 3.91 | 3.90  |
    +---------------------------------------------------------------------------+
</pre>

    Irrespective of the number of workers, we can see at 3 the CPU utilization was highest.
    Time taken to iterate through one work unit of size 3 (95^3) was approximately 2 minutes.
    A work unit of more than 3 is not feasible, as at 4 the number of combinations per worker is (95^4) which is too larger per worker.

2. Result of: scala project1.scala 4 (running for 1 minute)

<pre>
	snair &bj  00003299f3f28037590620546771c256ba66f33fa38c9df9a7fbddbd396c0c97
	snair )f]  0000ac7ffe5f92bade15a58f3e100b81fbd220c183ba46151903effa573fdf6b
	snair!.U@  0000c68fc44909148cbb8f2a95966d0885c586e0dc087f8c5016f92747fa8818
	snair .*a  0000cf448f3aa4c1fcf64c06c2bc17f688de69083546b105a5e744b2a3935829
	snair#.se  000014b59f6b4fbb9e912aa2d198a6f86128c2675c63fbab02a084fcae88007b
	snair!19R  00006a5a294de9aeb9c3c1f7ca0a081e35ebe7092751b88cbfa873fd0a2a6009
	snair"2^.  00004e89223678de25763762bca36b1544e829c9c526b557b3496624a3622b8d
	snair 2FH  0000f899b43f2024c9175dc7e4ee6d94e8a771f1904b5edc617f973bf5d019e4
	snair3|.   00000e13125ce026970e13d367d97378b6566854dbb90a0af02b1ad0e3e65b0b
	snair"5Nu  000041b6228679620b10edf81c6e0253a40e17774bf66995767f5b976f8dc891
	snair 6[k  0000852b1cea5a090ca9bb3faaade9e7459d37b607d9327a45ebc0e7e0e7690d
	snair!79*  000041cd240f783ac6b04159a31529f457864dccaf59ac32f521861da29fe33c
	snair 9)d  0000be4c524d68905c5e569b2eb91c6b88c7ba5f8ec8d9b3cfe83d0a27609093
	snair":MB  0000918bdca99b9d5d02f1a718372feb685d6c7a63a111b69a044ddf3c2243d1
	snair#:z4  0000631d8d308c455f88eaa8ab3daa860183a7a31acc430a4984977f33934cb2
	snair#;XH  000080a6d6d7abef4edd5f4a41ec8ecff5d4cb634907495b5ef7c018885e2c0e
	snair#;lz  0000a0db8a9f5c0336e2c39f241009d80d211218e1d4084f9a5df75045c9c75c
	snair =G'  0000daf3cb372d62bb8a11d9e341ab4902cf324db268f788e1d076cffd0fa0cb
	snair#?JR  00009285091425a881d34992270aa1aaa5e09233efb2817209cc33514cb21980
	snair#Bu+  0000370756abf7c5ca60cfff90306078684b1015011457946fafd381097efceb
	snair#CY"  00003b03f7682b2f16631df7d22f8fbe8422fde47498800559f0ea5617a5ecbb
	snair CfE  00001ea85c9d6c3bd7622dc8a0097b6bd3c84e967cb51f3a67ae9843795e122f
	snair GK"  0000a04267f92c1b5d2d707e26d27f3a3e5903d174cd5db2dfa262d1873d50d3
	snair!J$\  00004c68a187cbfba56d3c2232049a7ae2ae570c353a33b249503b091042cd05
	snair!J]0  0000d2dce02e002f5126f1dca1b04cfb53ea5179f2f33d91fbef1814ee1c3143
	snairGaf   000064ae9d66365a63c1b0667b0305242830485534b0d2f3e604250a2603f05b
	snair!Jwr  0000f5899a328d69d19214a28e00465be7c5b06b4b0fcae92d0b394f2125521e
	snair"JAJ  0000166a4dd71289b5a5c274c5388b864aa7183b02edf919c9252b9bfc3eb9a3
	snair MK7  0000244f47efb49eee2dd5b72f62f62843c97dc26c65917eb7c50c5e55c3cfe8
	snair#Oe5  000078a199cd20147bec85f9e083042ac823a293746fcd1c0cf69cd8dcfe08c0
	snair"Np\  0000d701259c7713ad3d63c915649cee253fc27433b11931f1eb92090a4a8574
	snairO?t   0000c638b37b23502f3f5c9bd5c7dd5e0cff99ba03893864c94ba14d435e319b
	snair"Q(0  0000d27385ce78c1269f4be576947e09221419ed815304def6bb882097f3c7a8
	snair!U`x  0000ee188855cc3524f728592e60fb45e11d404008f41498c2fa1cb228e4357f
	snairS$i   0000c34a2080e30e3f0b42a6f0e1dbc2b3f1a365691d51ca7456c735954be2a5
	snair#U]E  000027558372890bb6d3f99235573255e33cc3124141f774f9d3c18b42129f4c
	snair#Yco  0000bcb6b181e08c7e8dcc4a027e268fa699ed70c00dd45292b9d568d9df8084
	snair"Z99  0000f75694d70d96a21502b1cc65d905b357b0a16d6c95be1ebc628b6d98f36f
	snair"]c:  0000556d3b07cadbb9315b666721c736e03f17a150d471fe228848d47c46c3fe
	snair_;&   00006f855fd2a614c2d4e877dadbefad661dac102bc737227e31b18b07421361
	snair#f&X  00009cf7324726db61aae868a6aa48ce46420d42beb37edde3974af0e485bd78
	snair e+Q  000018afbae1014304c696459e34a7339b05ea3fcb5ef686b947329f294cad1d
	snair"gEU  0000cc1ba9a818d71464e1363235237bca0fab5418969061c15de4e49575d009
	snairfR9   00001cdf1fd9377c3250bf1850f61452640c24d7b72059bf402223636dd5cd0e
	snair"iKF  00004324f0a713f7cfcca6dc8e1318e3f75b1744c40307b6d4b00c87673ab484
	snair kEW  00001d012f0ecfe54724c6a2771f29a278c4d1313b48e4fd5bf7aec77a6c9e8c
	snairj!9   0000d30954830a4c9a26e9a157fe65b4ca1d062b3ea94e492c41a2713d025296
	snair"k{S  00008433d5db04098527396c07683b156ec282d8f3ffb80450a7d42222d3451b
	snairkZ-   00007ad49d9b2cf92b5defe2b331ed23fda62216c056801d928517784819e9ff
	snair#q_%  000074223876cbf3aeb394ea3fd2d70558079ca31f20ce4a274544fa37853695
	snairq ]   00008911aad5b2413f0700660505de539798213f55aaa5cd934b20e7ffbd11b8
	snair#tJe  00007c00fd6e6f369f21c067000e4772b0c03c212baaf7890519e6838bdae13e
	snair rD;  00004eedc964c8dd385f85a279c8247f975ac773b3ccf131ef8d91a8cf0da6e2
	snair"s`s  000065fff22efee9f2325168c7b66126ef20d7ee74df8d907bef26dc6ee3b0c2
	snair!v@A  0000291f079d009a4231d85223db842da42f9cde564342f256eb77864f784300
	snair!vA/  0000d68c24eb3043be26552deda3c27fe85fc172c1631775c9b6429697152d5c
	snair!y.X  000000e4b266f273eaeb60344eff841e2140c88c90cc7779a5269fdc5e264fd7
	snair"xBB  00000e0bd33aa0c00110b0c78f67b2ceeaefdd1b0c2cdf9551aee56d387a249a
	snair!y`8  000045b8082d5c3ca0864f82e6589587a412239effcfa0c4bcdd06c9ccf3af5f
	snair!z3/  0000d766765368d9eebffbf3c339a599749bfa7b0c6d14162ac1a817650704fa
	snair"z,/  0000d9149ee5771be7db4aa58b109ba82e61f511b77cbea40842527a131f804c
	snair"zJe  000042834d3d59c0d1df06fd883b32e84343f5455e842f1d1f14b4cb4bad6674
	snairx.7   000086f44f0e98fbca726aa9685afd6adfcf68952b388281d3b96b0c13edc484
	snair~*7   000096f7eee2a143548cfb4e69aa0d2a520ace47d0609e73a855d551bce4379b
	snair%#.S  0000e836c811c29132f6df9a557c458ccab20cfceb41858b2aba1f0252844fd1
	snair~KM   0000c47f373bd8e5e78c2b512fd01bfb289235c4b2570a91a7daf9c45cdf87a5
	snair%#kn  00002f879e13d4acbf6ba5d80bb27d25f30345173a9d6a19e4cbfd6093956fc6
	snair'%j^  0000080f8009b49ac9782f0bffaf9f375ae1e0bdd041be9ef27e818e2ef31ad1
	snair''&;  0000062d590d67c271cbe08c0e3aeb20a929eec3ae8489051811a681fde91786
	snair%(h{  00008bb10993c36c629e37edb92c9c2b68b7bb5dd9ce10a16cfb91aa8b03d507
	snair('#m  0000e0ed5f0ac7426bb4ee1aac10347709fb04f4d07ff2682282027c8c0fe19e
	snair'+bk  00006d73ee22a37d7e4b460775168e6f2015b5e1d54557698c52916470300ecf
	snair&,po  0000df864da1ba6afa18e781c0f7066c191eb5f5a1f8bcbf2d2271b708796e19
	snair'-FQ  0000c80089e66c67f169e20685aaa550602b0f61eb77770923e24e9cdfd200e5
	snair(+hA  00006b78b452e176bd322c2513283602fc9ebc4492212773e1abe33a3908df53
	snair(+tp  0000bd8116204b4054e125aa0ea0f1fa73c8c61403d157491695c59f8c4c12b3
	snair$2'\  000087f1ef81f2c94d8d64aebebf56bebd48551d0198c54aef911b14788beef7
	snair'1E`  0000d6d3af3d1a68de9d5db6e11a3acb07e4775c048813cf3ba7eb7342b91f4b
	snair(3(T  00003e3ea16a540eca79bbff77fbcb4d6b3d65452cc112b1f90e36fe478d6f9c
	snair(9,(  0000e10e33ce02267ad3a7c22caeb491536944a336d1229aad4c700a30ae8329
	snair'<'q  00006dfc9ef560f87b89950c5333315f09d179dfb312cd13c2ddd18d85a4dd57
	snair(=PA  000029fae7c8e9375b3f44daf3d1139ceeb0f85cd4faa5a32041c59b49ca2ddd
	snair%Cxv  000011cc2c94925ff35d29540f86efc08bf8d533b68cb3f81c0a169c66069eb8
	snair(A?6  00007571106e95553f33becf0deb406d684511183cc457e1c678bde7046dc6ab
	snair(AVm  0000be7e1c78c83f86f4430844a5614b1270b976ffbf7e3e0c5a6a661b39f9c5
</pre>

3. Result of:

    CPU: Intel(R) Core(TM) i3-4030U CPU @ 1.90GHz (fam: 06, model: 45, stepping: 01)
    Memory: 3895492K/4096180K available
    - time scala project1.scala 4
        [preethu@32-laptop Project1]$ time sbt 'run 4' > o
        real    1m36.378s
        user    5m48.174s
        sys     0m1.180s
    - time scala project1.scala 5
        [preethu@32-laptop Project1]$ time sbt 'run 5' > o
        real    1m40.175s
        user    5m59.172s
        sys     0m1.240s

4. Coin with most zeroes found:

    snair)|g7Z	000000089f87eef1c3fa529968e7f3c4e16a70aa45359fb51250c9bf85ea95fe

5. Largest number of working machines used:
