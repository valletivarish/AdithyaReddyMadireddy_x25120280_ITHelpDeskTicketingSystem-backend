# RDS PostgreSQL Configuration for the Backend Database
# Provisions a db.t3.micro PostgreSQL instance in a private subnet
# accessible only from the EC2 instance's security group.

# Database subnet group spanning two availability zones (required by RDS)
resource "aws_db_subnet_group" "helpdesk_db_subnet" {
  name       = "helpdesk-db-subnet-group"
  subnet_ids = [aws_subnet.private_subnet_a.id, aws_subnet.private_subnet_b.id]

  tags = {
    Name = "helpdesk-db-subnet-group"
  }
}

# Security group for RDS - only allows PostgreSQL traffic from EC2 instance
resource "aws_security_group" "rds_sg" {
  name        = "helpdesk-rds-sg"
  description = "Security group for the helpdesk RDS PostgreSQL instance"
  vpc_id      = aws_vpc.helpdesk_vpc.id

  # Allow PostgreSQL connections only from the EC2 security group
  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.ec2_sg.id]
    description     = "PostgreSQL access from EC2 only"
  }

  tags = {
    Name = "helpdesk-rds-sg"
  }
}

# RDS PostgreSQL database instance
resource "aws_db_instance" "helpdesk_db" {
  identifier             = "helpdesk-db"
  engine                 = "postgres"
  engine_version         = "15.4"
  instance_class         = "db.t3.micro"
  allocated_storage      = 20
  max_allocated_storage  = 50
  storage_type           = "gp3"
  db_name                = "helpdesk_db"
  username               = var.db_username
  password               = var.db_password
  db_subnet_group_name   = aws_db_subnet_group.helpdesk_db_subnet.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]
  publicly_accessible    = false
  skip_final_snapshot    = true
  multi_az               = false

  tags = {
    Name = "helpdesk-rds-postgres"
  }
}
