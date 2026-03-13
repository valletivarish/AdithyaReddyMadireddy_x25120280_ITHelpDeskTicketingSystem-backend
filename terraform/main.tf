# Main Terraform configuration for IT Help Desk Backend Infrastructure
# Configures the AWS provider and sets up core networking components
# including VPC, subnets, internet gateway, and route tables.

terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# AWS provider configuration using the specified region
provider "aws" {
  region = var.aws_region
}

# VPC for isolating the helpdesk application infrastructure
resource "aws_vpc" "helpdesk_vpc" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "helpdesk-vpc"
  }
}

# Public subnet for the EC2 instance (backend server)
resource "aws_subnet" "public_subnet" {
  vpc_id                  = aws_vpc.helpdesk_vpc.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "${var.aws_region}a"
  map_public_ip_on_launch = true

  tags = {
    Name = "helpdesk-public-subnet"
  }
}

# Private subnet for the RDS database instance (not publicly accessible)
resource "aws_subnet" "private_subnet_a" {
  vpc_id            = aws_vpc.helpdesk_vpc.id
  cidr_block        = "10.0.2.0/24"
  availability_zone = "${var.aws_region}a"

  tags = {
    Name = "helpdesk-private-subnet-a"
  }
}

# Second private subnet in a different AZ (required for RDS subnet group)
resource "aws_subnet" "private_subnet_b" {
  vpc_id            = aws_vpc.helpdesk_vpc.id
  cidr_block        = "10.0.3.0/24"
  availability_zone = "${var.aws_region}b"

  tags = {
    Name = "helpdesk-private-subnet-b"
  }
}

# Internet gateway to allow public internet access for the EC2 instance
resource "aws_internet_gateway" "helpdesk_igw" {
  vpc_id = aws_vpc.helpdesk_vpc.id

  tags = {
    Name = "helpdesk-igw"
  }
}

# Route table for public subnet directing traffic through the internet gateway
resource "aws_route_table" "public_rt" {
  vpc_id = aws_vpc.helpdesk_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.helpdesk_igw.id
  }

  tags = {
    Name = "helpdesk-public-rt"
  }
}

# Associate the public route table with the public subnet
resource "aws_route_table_association" "public_rta" {
  subnet_id      = aws_subnet.public_subnet.id
  route_table_id = aws_route_table.public_rt.id
}
