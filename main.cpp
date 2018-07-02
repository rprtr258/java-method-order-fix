#include <cctype>
#include <fstream>
#include <iostream>
#include <string>
#include <map>
#include <vector>
#include <set>
#include <algorithm>

using namespace std;

string getTab(string line) {
    int i = 0;
    while (i < line.length() && line[i] == ' ')
        i++;
    return line.substr(0, i);
}

string trimSpaces(string line) {
    int i = 0;
    while (i < line.length() && line[i] == ' ')
        i++;
    return line.substr(i);
}

string parseMethodName(string line) {
    int j = line.find('(') - 1;
    int i = j;
    while (line[i - 1] != ' ')
        i--;
    return line.substr(i, j - i + 1);
}

map<string, string> getMethodsAsMapFromFile(string filename) {
    string classname = filename.substr(0, filename.find('.'));
    ifstream fin(filename);
    bool readingMethod = false;
    string methodEndLine;
    string methodName;
    string methodDeclaration;
    string methodBody;
    map<string, string> result;
    while (!fin.eof()) {
        string line;
        getline(fin, line);
        string lineCopy = line;
        line = trimSpaces(line);
        if ((line.find("public") == 0 || line.find("private") == 0) &&
             line.back() != ';' &&
             line.find(classname) == -1) {
            methodDeclaration = lineCopy;
            methodName = parseMethodName(line);
            readingMethod = true;
            methodEndLine = getTab(lineCopy) + "}";
        } else if (readingMethod && lineCopy == methodEndLine) {
            result[methodName] = methodDeclaration + "\n" + methodBody;
            methodBody = "";
            readingMethod = false;
        } else if (readingMethod) {
            methodBody += lineCopy + "\n";
        }
    }
    fin.close();
    return result;
}

vector<string> split(string str) {
    vector<string> result;
    string tmp;
    for (char c : str) {
        if (isalpha(c)) {
            tmp += c;
        } else {
            result.push_back(tmp);
            tmp = "";
        }
    }
    return result;
}

map<string, set<string>> buildGraph(map<string, string> data) {
    map<string, set<string>> graph;
    for (pair<string, string> e : data) {
        vector<string> words = split(e.second);
        for (string word : words)
            if (data.count(word) == 1 && word != e.first)
                graph[e.first].insert(word);
    }
    return graph;
}

vector<string> tsort;

void dfs(map<string, set<string>> &graph, string method, map<string, bool> &vis) {
    if (vis[method])
        return;
    vis[method] = true;
    for (auto e : graph[method])
        dfs(graph, e, vis);
    tsort.push_back(method);
}

void dfs(map<string, set<string>> &graph) {
    map<string, bool> vis;
    for (auto e : graph)
        vis[e.first] = false;
    for (auto e : graph)
        dfs(graph, e.first, vis);
    reverse(tsort.begin(), tsort.end());
}

int main(int argc, char **argv) {
    if (argc != 2) {
        cout << "Java file was not specified." << endl;
        return 0;
    }
    string filename = argv[1];
    map<string, string> data = getMethodsAsMapFromFile(filename);
    map<string, set<string>> graph = buildGraph(data);
    dfs(graph);
    for (string method : tsort)
        cout << method << endl;
    return 0;
}
