# COP5615_Projects

## Project 1

### Structure
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

