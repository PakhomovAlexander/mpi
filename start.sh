export MPJ_HOME=lib/mpj-v0_44
export PATH=$MPJ_HOME/bin:$PATH

mpjrun.sh -np 4 -cp build/classes/java/main task1.Task1
