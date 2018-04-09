# Ansible Playbooks to Deploy Sheepdog with WebGoat7

These two ansible playbooks automate the deployment and cleanup of Sheepdog and WebGoat7 to quickly setup a demo environment using the Contrast Security Secure DevOps Platform.

**Note:** You must download and copy your contrast.jar file to the same directory where webgoat7 and sheepdog are installed. The default directory is ``` ~/webgoat7```.

## To use this playbook, follow the instructions below

### 1. Clone this github repo.
```
$ git clone git@github.com:Contrast-Security-OSS/sheepdog.git
```
### 2. Run the ansible playbook
```
$ ansible-playbook ./sheepdog/ansible/main.yml
```

### 3. Execute attack.sh
```
$ cd ~/webgoat7
$ ./attack.sh
```

**Note:** ```attack.sh``` will take approximatley 30 minutes to run

## To start webgoat7 after ```attack.sh``` is finished
```
$ ./webgoat.sh
```

## To cleanup
```
ansible-playbook ./sheepdog/ansible/cleanup.yml
```
