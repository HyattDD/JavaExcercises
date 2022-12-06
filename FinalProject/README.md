# Kokit Project

## Main Functions

#### kokit init
- `kokit init`
  - init in `.git` directory is not allowed 
  - if the present directory hasn't been initialized, then make an directory named `.git` , which has an empty directory named `objects` in it.
  - else reinitialized
  

#### kokit add
- `kokit add <file name\>`
  - if file exists, add successfully
  - else fail
- `kokit add <directory name\>`
  - add all the files in dir recursively
- `kokit add  .`
  - add all the files in present path

#### kokit commit

## Implementation

#### kokit init

#### kokit add

- add file
  - map the file

#### kokit commit


有一个比较大的bug没有解决，就是当找不到.corgit文件夹时会一层层向上找，但是会出问题
