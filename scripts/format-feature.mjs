#!/usr/bin/env node

import fs from 'node:fs/promises'
import path from 'node:path'
import process from 'node:process'
import { fileURLToPath } from 'node:url'

import { AstBuilder, GherkinClassicTokenMatcher, Parser } from '@cucumber/gherkin'
import { pretty } from '@cucumber/gherkin-utils'
import { IdGenerator } from '@cucumber/messages'

function parse(source, language) {
  return new Parser(
    new AstBuilder(IdGenerator.uuid()),
    new GherkinClassicTokenMatcher(language)
  ).parse(source)
}

function examplesKeywords(document) {
  const keywords = new Set()

  function visitChildren(children = []) {
    for (const child of children) {
      if (child.scenario) {
        for (const examples of child.scenario.examples) {
          keywords.add(examples.keyword.trim())
        }
      }
      if (child.rule) {
        visitChildren(child.rule.children)
      }
    }
  }

  visitChildren(document.feature?.children)
  return [...keywords]
}

function escapeRegExp(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

function removeBlankLineBeforeExamples(source, keywords) {
  if (keywords.length === 0) {
    return source
  }

  const alternatives = keywords.map(escapeRegExp).join('|')
  const examplesLine = new RegExp(
    `\\n\\n(?=(?:[ \\t]*@[^\\n]*\\n)*[ \\t]*(?:${alternatives})\\s*:)`,
    'gu'
  )
  return source.replace(examplesLine, '\n')
}

export function formatFeature(source) {
  const document = parse(source)
  const formatted = pretty(document)
  const projectFormatted = removeBlankLineBeforeExamples(
    formatted,
    examplesKeywords(document)
  )

  // Keep the upstream formatter's safety property: never emit invalid Gherkin.
  parse(projectFormatted, document.feature?.language)
  return projectFormatted
}

async function readStdin() {
  const chunks = []
  for await (const chunk of process.stdin) {
    chunks.push(chunk)
  }
  return Buffer.concat(chunks).toString('utf8')
}

async function main(files) {
  if (files.length === 0) {
    process.stdout.write(formatFeature(await readStdin()))
    return
  }

  for (const file of files) {
    const source = await fs.readFile(file, 'utf8')
    await fs.writeFile(file, formatFeature(source), 'utf8')
  }
}

const invokedPath = process.argv[1] ? path.resolve(process.argv[1]) : undefined
if (invokedPath === fileURLToPath(import.meta.url)) {
  main(process.argv.slice(2)).catch((error) => {
    console.error(error)
    process.exitCode = 1
  })
}
