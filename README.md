# Kalah AI
## An MCTS AI we created for a university course. It plays the famous game, Kalah.

This project is developed by Feifan Chen(陈非凡), Xiaotan Zhu(朱笑谈), Yirong Yu(俞奕戎), and Wenqing Zong(宗文卿).

## The Kalah Game

See a brief introduction [here](https://en.wikipedia.org/wiki/Kalah). But in this project, we are playing the 7 * 7 version. [Pie rule](https://en.wikipedia.org/wiki/Pie_rule) is applied here to cancle out first player advantage.

## Our Approach

We used [MCTS](https://en.wikipedia.org/wiki/Monte_Carlo_tree_search) algorithm with some improvement to implement our AI agent. 

Our improvement follows these research papers:

* [An analysis for strength improvement of an MCTS-based program playing Chinese dark chess](https://www.sciencedirect.com/science/article/pii/S0304397516302705#se0100)

* [Case Study - Kalah](https://www.cs.drexel.edu/~jpopyack/Courses/AI/Sp15/notes/CaseStudy_Kalah.pdf)

* [Parallel Monte-Carlo Tree Search](https://dke.maastrichtuniversity.nl/m.winands/documents/multithreadedMCTS2.pdf)

* [MCTS-Minimax_Hybrids](https://dke.maastrichtuniversity.nl/m.winands/documents/mcts-minimax_hybrids_final.pdf)

* [Trade-Offs in Sampling-Based Adversarial Planning](https://aaai.org/ocs/index.php/ICAPS/ICAPS11/paper/view/2708/3154)

* [Monte Carlo Tree Search with heuristic evaluations using implicit minimax backups - IEEE Conference Publication](https://ieeexplore.ieee.org/document/6932903)

    

## How To Run Our Code

1. Please download and open our project in IntelliJ and accept all default settings. 

2. Open IntelliJ, click the green "Run" button.

3. Terminate it, we merelly want IntelliJ to build our code.

4. In `Test_Agents` folder, run `./compile.sh` for MacOS and Linux, or `.\compile.bat` for Windows.

5. * If you want to see two agents playing agaist each other, in `Test_Agents` folder, do:

    ```shell
    java -jar ManKalah.jar "java -jar <FirstAgent>" "java -jar <SecondAgent>"
    ```

    where `<FirstAgent>` and `<SecondAgent>` can be (and must be) replaced by a jar file in `Test_Agents` folder.

    * If you are lazy and you are using `MacOS` or `Linux`, simply do:

        ```shell
        ./run
        ```

        This will test our AI agent again all four test agents with both being player 1 and player 2.

    * If you want to play against our agent (or any other agent), open a ternimal, do:
   
        ```shell
        nc localhost 12345
        ```
   
        and then in `Test_Agents` folder, do:
   
        ```shell
        java -jar ManKalah.jar "java -jar <Agent>" "nc localhost 12345"
        ```
   
        The first argument indicates the first player, the second indicates the second player. For our protocal, please see `doc` folder for more details.