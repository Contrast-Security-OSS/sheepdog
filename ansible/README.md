# Ansible Playbooks to Deploy Sheepdog with WebGoat7

These two ansible playbooks automate the deployment and cleanup of Sheepdog and WebGoat7 to quickly setup a demo environment using the Contrast Security Secure DevOps Platform.

## To use this playbook, follow the instructions below

### Create a '~.contrast.cfg file'
The playbook relies on API credentials in this file.
```
username: [username]
service_key: [service_key]
teamserver_url: [teamserver_url]
teamserver_organization: [teamserver_organization]
teamserver_url: [teamserver_url]
api_key: [api_key]

#These might not be required for the playbook to work.
#@todo Validate the need for these fields
agent_username: [agent_username]
agent_service_key: [agent_service_key]
```

### Clone this github repo.
```
$ git clone git@github.com:Contrast-Security-OSS/sheepdog.git
```
### Run the ansible playbook
```
$ ansible-playbook ./sheepdog/ansible/main.yml
```

### Execute attack.sh
**Note:** ```attack.sh``` will take approximatley 30 minutes to run

```
$ cd ~/webgoat7
$ ./attack.sh
```


### Demo: Start webgoat7 attack is done
```
$ ./webgoat.sh
```

Then browse to webgoat at http://localhost:8080/WebGoat/

## Cleanup
```
ansible-playbook ./sheepdog/ansible/cleanup.yml
```
