# Input variables for the Terraform configuration
# These values should be provided via terraform.tfvars or environment variables

variable "aws_region" {
  description = "AWS region for deploying the infrastructure"
  type        = string
  default     = "eu-west-1"
}

variable "instance_type" {
  description = "EC2 instance type for the backend server"
  type        = string
  default     = "t3.micro"
}

variable "ami_id" {
  description = "AMI ID for the EC2 instance (Amazon Linux 2023)"
  type        = string
  default     = "ami-0c38b837cd80f13bb"
}

variable "key_pair_name" {
  description = "Name of the SSH key pair for EC2 access"
  type        = string
  default     = "helpdesk-key"
}

variable "db_username" {
  description = "Username for the RDS PostgreSQL database"
  type        = string
  default     = "helpdesk_admin"
  sensitive   = true
}

variable "db_password" {
  description = "Password for the RDS PostgreSQL database"
  type        = string
  sensitive   = true
}

variable "s3_bucket_name" {
  description = "S3 bucket name for hosting the frontend application"
  type        = string
  default     = "helpdesk-frontend-app"
}
