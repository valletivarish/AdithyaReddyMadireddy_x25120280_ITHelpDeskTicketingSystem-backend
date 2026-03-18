# EC2 Instance Configuration for the Backend API Server
# Provisions a t2.micro instance with Java 17, systemd service configuration,
# and a security group allowing HTTP (8080) and SSH (22) access.
# Includes an Elastic IP for a persistent public IP address.

# Security group for the EC2 instance - controls inbound/outbound traffic
resource "aws_security_group" "ec2_sg" {
  name        = "helpdesk-ec2-sg"
  description = "Security group for the helpdesk backend EC2 instance"
  vpc_id      = aws_vpc.helpdesk_vpc.id

  # Allow SSH access for deployment and management
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "SSH access for deployment"
  }

  # Allow HTTP access on port 8080 for the Spring Boot API
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Backend API access"
  }

  # Allow all outbound traffic for internet access
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = {
    Name = "helpdesk-ec2-sg"
  }
}

# EC2 instance running the Spring Boot backend application
resource "aws_instance" "helpdesk_backend" {
  ami                    = var.ami_id
  instance_type          = var.instance_type
  key_name               = var.key_pair_name
  subnet_id              = aws_subnet.public_subnet.id
  vpc_security_group_ids = [aws_security_group.ec2_sg.id]

  # User data script runs on first boot to install Java and configure the service
  user_data = <<-EOF
              #!/bin/bash
              # Update system packages
              sudo apt-get update -y
              # Install Java 17
              sudo apt-get install -y openjdk-17-jre-headless
              # Create application directory
              sudo mkdir -p /opt/helpdesk-api
              sudo chown ubuntu:ubuntu /opt/helpdesk-api
              # Create systemd service file for the backend application
              cat <<'SERVICE' | sudo tee /etc/systemd/system/helpdesk-api.service
              [Unit]
              Description=IT Help Desk Backend API
              After=network.target

              [Service]
              Type=simple
              User=ubuntu
              WorkingDirectory=/opt/helpdesk-api
              ExecStart=/usr/bin/java -jar /opt/helpdesk-api/helpdesk-api-1.0.0.jar --spring.profiles.active=prod
              Restart=always
              RestartSec=10

              [Install]
              WantedBy=multi-user.target
              SERVICE
              # Enable the service to start on boot
              sudo systemctl daemon-reload
              sudo systemctl enable helpdesk-api
              EOF

  tags = {
    Name = "helpdesk-backend-server"
  }
}

# Elastic IP for the EC2 instance - provides a static public IP address
resource "aws_eip" "helpdesk_eip" {
  instance = aws_instance.helpdesk_backend.id
  domain   = "vpc"

  tags = {
    Name = "helpdesk-backend-eip"
  }

  depends_on = [aws_internet_gateway.helpdesk_igw]
}
