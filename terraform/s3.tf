# S3 Bucket Configuration for Static Website Hosting
# This bucket hosts the React frontend application as a static website.
# CloudFront or direct S3 website URL provides public access.

resource "aws_s3_bucket" "helpdesk_frontend" {
  bucket = var.s3_bucket_name

  tags = {
    Name = "helpdesk-frontend-bucket"
  }
}

# Enable static website hosting on the S3 bucket
resource "aws_s3_bucket_website_configuration" "frontend_website" {
  bucket = aws_s3_bucket.helpdesk_frontend.id

  index_document {
    suffix = "index.html"
  }

  error_document {
    key = "index.html"
  }
}

# Make the bucket publicly readable for website hosting
resource "aws_s3_bucket_public_access_block" "frontend_public_access" {
  bucket = aws_s3_bucket.helpdesk_frontend.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

# Bucket policy to allow public read access to all objects
resource "aws_s3_bucket_policy" "frontend_policy" {
  bucket = aws_s3_bucket.helpdesk_frontend.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "PublicReadGetObject"
        Effect    = "Allow"
        Principal = "*"
        Action    = "s3:GetObject"
        Resource  = "${aws_s3_bucket.helpdesk_frontend.arn}/*"
      }
    ]
  })

  depends_on = [aws_s3_bucket_public_access_block.frontend_public_access]
}
